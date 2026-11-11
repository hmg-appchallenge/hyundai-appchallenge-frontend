package com.hmgappchallenge.kidsrhythm.audio

import com.hmgappchallenge.kidsrhythm.server.SongService
import com.hmgappchallenge.kidsrhythm.server.VoiceEmbedResult
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 가족 목소리 등록 (S02-2·3): 정해진 시간 동안 녹음하고, 끝나면 서버에서 목소리 특징을 받는다. */
class VoiceEnrollment(private val recorder: PcmRecorder, private val service: SongService) {
    private val buffer = ByteArrayOutputStream()

    fun hasPermission(): Boolean = recorder.hasPermission()

    /** 녹음을 시작한다. 마이크를 열 수 없으면 false. */
    fun start(): Boolean {
        synchronized(buffer) { buffer.reset() }
        return recorder.start(
            object : PcmRecorder.Listener {
                override fun onChunk(pcm: ByteArray) {
                    synchronized(buffer) { buffer.write(pcm) }
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
