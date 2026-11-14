package com.hmgappchallenge.kidsrhythm.recording

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hmgappchallenge.kidsrhythm.audio.InstrumentSounds
import com.hmgappchallenge.kidsrhythm.audio.PcmRecorder
import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.server.BeatInfo
import com.hmgappchallenge.kidsrhythm.server.DriveInfo
import com.hmgappchallenge.kidsrhythm.server.ServerException
import com.hmgappchallenge.kidsrhythm.server.SessionMember
import com.hmgappchallenge.kidsrhythm.server.SongService
import java.time.OffsetDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * S04 녹음 한 번. 마이크 녹음 조각을 2초마다 서버로 보내고,
 * 돌아온 결과로 '연주 중'인 가족과 '지금까지 들린 단어'를 갱신한다 (docs/API.md 3번).
 * 상태는 Compose 상태라 화면이 그대로 읽는다.
 */
class RecordingController(
    private val service: SongService,
    private val recorder: PcmRecorder,
    private val sounds: InstrumentSounds?,
    private val scope: CoroutineScope,
    private val appScope: CoroutineScope,
) {
    enum class Phase { Idle, Connecting, Recording, Finishing, Done, Failed }

    var phase by mutableStateOf(Phase.Idle)
        private set
    var isPaused by mutableStateOf(false)
        private set
    var playingMemberIds by mutableStateOf<Set<Long>>(emptySet())
        private set
    val heardWords = mutableStateListOf<String>()
    val levelHeights = mutableStateListOf<Int>()
    var recordedMillis by mutableLongStateOf(0L)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var warning by mutableStateOf<String?>(null)
        private set

    /** 처음 들린 명사 (의성어 말고). 서버도 명사로 노래 제목을 지어서 S05 "○○ 노래를 만들고 있어요"에 쓴다. */
    var firstNoun: String? = null
        private set

    private var members: List<FamilyMember> = emptyList()
    private var sessionId: String? = null
    private var seq = 0
    private var chunks = Channel<ByteArray>(Channel.UNLIMITED)
    private var sender: Job? = null
    private var ticker: Job? = null
    private val speakingUntil = mutableMapOf<Long, Long>()
    private val speedSamples = mutableListOf<Float>()
    private var outsideTemp: Float? = null
    private var startedAt = OffsetDateTime.now().toString()
    private var maxRecordingMillis = DEFAULT_MAX_RECORDING_MS

    /** 녹음할 수 있는 시간을 다 썼는지. 다 쓰면 멈춘 채로 완성하기만 할 수 있다. */
    val isAtLimit: Boolean get() = recordedMillis >= maxRecordingMillis

    /** 완성하기 요청만 실패해서 녹음을 버리지 않고 다시 보낼 수 있는지. */
    var canRetryFinish by mutableStateOf(false)
        private set
    private var lastBeat: BeatInfo? = null

    /** 1초마다 읽는 지금 차량 상태 (속도 km/h, 바깥 온도). */
    var vehicle: () -> Pair<Float, Float?> = { 0f to null }

    /** 녹음을 시작한다. 실패한 뒤 다시 부르면 새 세션으로 처음부터 녹음한다. */
    fun start(family: List<FamilyMember>) {
        if (phase != Phase.Idle && phase != Phase.Failed) return
        members = family
        resetForNewSession()
        phase = Phase.Connecting
        scope.launch {
            val info = try {
                service.startSession(family.map { SessionMember(it.id, it.instrument.name, it.voiceEmbedding) })
            } catch (e: ServerException) {
                fail(if (e.isNetwork) "노래 서버에 연결할 수 없어요. 서버가 켜져 있는지 확인해 주세요." else e.message ?: "서버 오류")
                return@launch
            }
            sessionId = info.sessionId
            if (info.maxRecordingSec > 0) maxRecordingMillis = info.maxRecordingSec * 1_000L
            val out = chunks
            val ok = recorder.start(object : PcmRecorder.Listener {
                override fun onLevel(db: Float) {
                    scope.launch(Dispatchers.Main) { addLevel(db) }
                }

                override fun onChunk(pcm: ByteArray) {
                    out.trySend(pcm)
                }

                override fun onError(message: String) {
                    scope.launch(Dispatchers.Main) {
                        if (phase == Phase.Recording) {
                            fail(message)
                            cancelSessionQuietly()
                        }
                    }
                }
            }, info.chunkMs)
            if (!ok) {
                fail("마이크를 열 수 없어요. 마이크 권한을 확인해 주세요.")
                cancelSessionQuietly()
                return@launch
            }
            phase = Phase.Recording
            sender = scope.launch { sendLoop(info.sessionId) }
            ticker = scope.launch { tickLoop() }
        }
    }

    private fun resetForNewSession() {
        // 앞 세션의 조각 보내기가 남아 있으면 끝내고, 서버에 남은 세션도 지운다
        chunks.close()
        sender?.cancel()
        ticker?.cancel()
        cancelSessionQuietly()
        canRetryFinish = false
        lastBeat = null
        errorMessage = null
        warning = null
        isPaused = false
        recorder.isPaused = false
        sessionId = null
        seq = 0
        chunks = Channel(Channel.UNLIMITED)
        speakingUntil.clear()
        playingMemberIds = emptySet()
        speedSamples.clear()
        outsideTemp = null
        heardWords.clear()
        firstNoun = null
        levelHeights.clear()
        recordedMillis = 0L
        maxRecordingMillis = DEFAULT_MAX_RECORDING_MS
        startedAt = OffsetDateTime.now().toString()
    }

    private fun addLevel(db: Float) {
        if (phase != Phase.Recording || isPaused) return
        recordedMillis += PcmRecorder.BLOCK_MS
        // -60 dBFS(조용함) ~ -10 dBFS(큰 소리)를 막대 높이 12~84로
        val t = ((db + 60f) / 50f).coerceIn(0f, 1f)
        levelHeights.add((12 + 72 * t).toInt())
        if (levelHeights.size > MAX_BARS) levelHeights.removeRange(0, levelHeights.size - MAX_BARS)
        if (isAtLimit) stopAtLimit()
    }

    /** 녹음할 수 있는 시간을 다 쓰면 멈추고 완성하기를 안내한다 (서버도 넘치는 조각은 받지 않는다). */
    private fun stopAtLimit() {
        warning = "녹음은 ${maxRecordingMillis / 60_000}분까지예요. 완성하기를 눌러 주세요."
        recorder.isPaused = true
        isPaused = true
        speakingUntil.clear()
        playingMemberIds = emptySet()
    }

    private suspend fun sendLoop(id: String) {
        for (pcm in chunks) {
            var attempt = 0
            while (true) {
                try {
                    val r = service.sendChunk(id, seq, pcm)
                    // 연결 안내만 지운다 (3분을 다 썼다는 안내는 완성할 때까지 둔다)
                    if (warning == CONNECTION_WARNING) warning = null
                    val now = System.currentTimeMillis()
                    val before = playingMemberIds
                    r.speaking.forEach { speakingUntil[it] = now + SPEAKING_HOLD_MS }
                    refreshSpeaking(now)
                    (playingMemberIds - before).forEach { mid -> members.firstOrNull { it.id == mid }?.let { sounds?.hit(it.instrument) } }
                    r.newWords.forEach { w ->
                        if (w.text !in heardWords) heardWords.add(w.text)
                        if (firstNoun == null && w.kind == "word") firstNoun = w.text
                    }
                    break
                } catch (e: ServerException) {
                    if (e.code == "TOO_LONG") {
                        stopAtLimit()
                        break
                    }
                    if (e.code == "NOT_FOUND") {
                        fail("녹음 세션이 끝났어요. 다시 녹음해 주세요.")
                        return
                    }
                    attempt++
                    if (!isAtLimit) warning = CONNECTION_WARNING
                    if (attempt >= 3) break
                    delay(500L * attempt)
                }
            }
            seq++
        }
    }

    private fun refreshSpeaking(now: Long) {
        playingMemberIds = speakingUntil.filterValues { it > now }.keys
    }

    private suspend fun tickLoop() {
        var lastSample = 0L
        while (scope.isActive && phase == Phase.Recording) {
            val now = System.currentTimeMillis()
            refreshSpeaking(now)
            if (!isPaused && now - lastSample >= 1_000) {
                val (speed, temp) = vehicle()
                speedSamples.add(speed)
                if (temp != null) outsideTemp = temp
                lastSample = now
            }
            delay(250)
        }
    }

    fun togglePause() {
        if (phase != Phase.Recording) return
        // 시간을 다 쓴 뒤에는 다시 녹음할 수 없다
        if (isPaused && isAtLimit) return
        isPaused = !isPaused
        recorder.isPaused = isPaused
        if (isPaused) {
            speakingUntil.clear()
            playingMemberIds = emptySet()
        }
    }

    fun pause() {
        if (phase == Phase.Recording && !isPaused) togglePause()
    }

    /** 완성하기. 남은 조각을 다 보내고 노래 작업 번호를 돌려준다. 실패하면 null. */
    suspend fun complete(bpm: Int, beatType: Int): String? {
        val id = sessionId ?: return null
        if (phase != Phase.Recording) return null
        phase = Phase.Finishing
        // stop()은 녹음 스레드가 끝날 때까지 기다리므로 화면 스레드를 막지 않게 한다
        withContext(Dispatchers.IO) { recorder.stop() }
        chunks.close()
        withTimeoutOrNull(20_000) { sender?.join() }
        ticker?.cancel()
        lastBeat = BeatInfo(bpm, beatType)
        return finishSession(id)
    }

    /**
     * 완성하기 요청만 실패했을 때(서버 연결 끊김 등) 녹음은 그대로 두고 다시 보낸다.
     * 서버는 세션을 10분 동안 들고 있다 (docs/API.md 기본 원칙).
     */
    suspend fun retryFinish(): String? {
        val id = sessionId ?: return null
        if (phase != Phase.Failed || !canRetryFinish) return null
        phase = Phase.Finishing
        return finishSession(id)
    }

    private suspend fun finishSession(id: String): String? {
        val beat = lastBeat ?: return null
        canRetryFinish = false
        return try {
            val songId = service.finish(id, DriveInfo(speedSamples.toList(), 1f, outsideTemp, startedAt), beat)
            phase = Phase.Done
            songId
        } catch (e: ServerException) {
            // 429(RATE_LIMITED)는 서버 문장을 그대로 보여 준다. 세션이 10분 남아 있어 잠시 뒤 다시 완성할 수 있다
            canRetryFinish = e.isNetwork || e.httpStatus == 429 || e.httpStatus >= 500
            fail(if (e.isNetwork) "노래 서버에 연결할 수 없어요. 서버를 확인하고 다시 완성해 주세요." else e.message ?: "서버 오류")
            null
        }
    }

    /** 뒤로 가기 등으로 그만둘 때. 서버가 모아 둔 녹음을 바로 지운다. */
    fun cancel() {
        if (phase == Phase.Done) return
        recorder.stop()
        chunks.close()
        sender?.cancel()
        ticker?.cancel()
        cancelSessionQuietly()
        phase = Phase.Idle
    }

    private fun cancelSessionQuietly() {
        val id = sessionId ?: return
        sessionId = null
        appScope.launch {
            try {
                service.cancelSession(id)
            } catch (_: ServerException) {
            }
        }
    }

    private fun fail(message: String) {
        errorMessage = message
        phase = Phase.Failed
        recorder.stop()
    }

    /** 화면에 그릴 순간 상태. */
    fun snapshot(elapsedMillis: Long) = RecordingSnapshot(
        elapsedMillis = elapsedMillis,
        levelHeights = levelHeights.toList(),
        playingMemberIds = playingMemberIds,
        heardWords = heardWords.toList(),
    )

    private companion object {
        const val MAX_BARS = 200
        const val SPEAKING_HOLD_MS = 2_600L
        const val DEFAULT_MAX_RECORDING_MS = 180_000L
        const val CONNECTION_WARNING = "서버 연결이 불안정해요. 다시 보내는 중..."
    }
}
