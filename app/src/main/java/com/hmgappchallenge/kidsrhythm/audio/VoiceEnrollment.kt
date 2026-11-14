package com.hmgappchallenge.kidsrhythm.audio

import com.hmgappchallenge.kidsrhythm.server.SongService
import com.hmgappchallenge.kidsrhythm.server.VoiceEmbedResult
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 가족 목소리 등록 (S02-2·3): 정해진 시간 동안 녹음하고, 끝나면 서버에서 목소리 특징을 받는다. */
class VoiceEnrollment(private val recorder: PcmRecorder, private val service: SongService) {
    private val buffer = ByteArrayOutputStream()
    private val main = Handler(Looper.getMainLooper())

    /** 녹음하는 동안 0.1초마다 쌓이는 목소리 크기 막대 높이(dp). 최근 [MAX_BARS]개만 둔다. */
    val levelHeights = mutableStateListOf<Int>()

    fun hasPermission(): Boolean = recorder.hasPermission()

    /** 녹음을 시작한다. 마이크를 열 수 없으면 false. */
    fun start(): Boolean {
        synchronized(buffer) { buffer.reset() }
        levelHeights.clear()
        return recorder.start(
            object : PcmRecorder.Listener {
                override fun onChunk(pcm: ByteArray) {
                    synchronized(buffer) { buffer.write(pcm) }
                }

                override fun onLevel(db: Float) {
                    val height = levelBarHeight(db)
                    main.post {
                        levelHeights.add(height)
                        if (levelHeights.size > MAX_BARS) levelHeights.removeRange(0, levelHeights.size - MAX_BARS)
                    }
                }
            },
            chunkMillis = 500,
        )
    }

    /** 녹음을 끝내고 서버로 보낸다. 서버는 받은 녹음을 바로 지운다. */
    suspend fun finish(): VoiceEmbedResult {
        withContext(Dispatchers.IO) { recorder.stop() }
        val pcm = synchronized(buffer) { buffer.toByteArray().also { buffer.reset() } }
        return service.embedVoice(pcm16ToWav(pcm))
    }

    fun cancel() {
        recorder.stop()
        synchronized(buffer) { buffer.reset() }
    }
}

/** 화면에 남겨 둘 막대 수. 파형 폭(603)을 다 채우고 남을 만큼. */
private const val MAX_BARS = 60

/** -60 dBFS(조용함) ~ -10 dBFS(큰 소리)를 막대 높이 8~45로 바꾼다. */
internal fun levelBarHeight(db: Float): Int {
    val t = ((db + 60f) / 50f).coerceIn(0f, 1f)
    return (8 + 37 * t).toInt()
}
