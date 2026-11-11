package com.hmgappchallenge.kidsrhythm.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * 마이크 녹음. PCM 16비트, 16kHz, 모노로 받아서 0.1초마다 소리 크기를, 2초마다 녹음 조각을 넘긴다.
 * 콜백은 녹음 스레드에서 불린다.
 *
 * 디버그 빌드에서 앱 외부 저장소(files/)에 debug_mic.wav가 있으면 마이크 대신 그 파일을 실제 시간처럼 흘려보낸다
 * (에뮬레이터에서 사람 목소리 없이 전체 흐름을 시험하려고).
 */
class PcmRecorder(private val context: Context) {

    interface Listener {
        /** 0.1초마다. 소리 크기 dBFS (-90 ~ 0). 멈춘 동안에는 불리지 않는다. */
        fun onLevel(db: Float) {}

        /** 녹음 조각 (멈춘 동안의 소리는 빠진다). */
        fun onChunk(pcm: ByteArray) {}

        /** 녹음 도중 마이크를 더 쓸 수 없게 됐을 때 (다른 앱이 마이크를 가져간 경우 등). */
        fun onError(message: String) {}
    }

    @Volatile
    private var running = false

    @Volatile
    var isPaused = false

    private var thread: Thread? = null

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    /** 녹음을 시작한다. 권한이 없거나 마이크를 열 수 없으면 false. 마이크는 여기서 바로 열어 실패를 알 수 있게 한다. */
    fun start(listener: Listener, chunkMillis: Int = CHUNK_MS): Boolean {
        if (running) return true
        val debugFile = debugInput()
        val body: Runnable
        if (debugFile != null) {
            body = Runnable { runFromFile(debugFile, listener, chunkMillis) }
        } else {
            if (!hasPermission()) return false
            val record = openMic() ?: return false
            body = Runnable { runFromMic(record, listener, chunkMillis) }
        }
        running = true
        isPaused = false
        thread = Thread(body, "pcm-recorder").apply { start() }
        return true
    }

    /** 멈추고 남은 소리를 마지막 조각으로 넘긴다. 녹음 스레드가 끝날 때까지 기다린다. */
    fun stop() {
        running = false
        thread?.join(1_500)
        thread = null
    }

    private fun debugInput(): File? {
        val debuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!debuggable) return null
        return context.getExternalFilesDir(null)?.let { File(it, DEBUG_FILE) }?.takeIf { it.exists() }
    }

    @SuppressLint("MissingPermission") // start()에서 권한을 확인한다
    private fun openMic(): AudioRecord? {
        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val record = try {
            AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf, BLOCK_BYTES * 4))
        } catch (e: Exception) {
            Log.w(TAG, "마이크를 열 수 없어요", e)
            return null
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            Log.w(TAG, "마이크를 초기화하지 못했어요")
            record.release()
            return null
        }
        return record
    }

    private fun runFromMic(record: AudioRecord, listener: Listener, chunkMillis: Int) {
        val block = ByteArray(BLOCK_BYTES)
        val chunker = Chunker(listener, chunkMillis)
        try {
            try {
                record.startRecording()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "녹음을 시작하지 못했어요", e)
            }
            if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                running = false
                listener.onError("마이크를 쓸 수 없어요. 다른 앱이 마이크를 쓰고 있는지 확인해 주세요.")
                return
            }
            while (running) {
                var read = 0
                while (read < block.size && running) {
                    val n = record.read(block, read, block.size - read)
                    if (n < 0) {
                        // ERROR_INVALID_OPERATION, ERROR_DEAD_OBJECT 등: 더 읽을 수 없다
                        Log.w(TAG, "마이크 읽기 오류 $n")
                        running = false
                        listener.onError("마이크 연결이 끊겼어요. 다시 녹음해 주세요.")
                        break
                    }
                    if (n == 0) {
                        Thread.sleep(10)
                        continue
                    }
                    read += n
                }
                if (read > 0 && !isPaused) chunker.add(block, read)
            }
        } catch (_: InterruptedException) {
        } finally {
            try { record.stop() } catch (_: IllegalStateException) {}
            record.release()
            chunker.flush()
        }
    }

    private fun runFromFile(file: File, listener: Listener, chunkMillis: Int) {
        val pcm = readPcm16Wav(file)
        if (pcm == null || pcm.isEmpty()) {
            running = false
            listener.onError("$DEBUG_FILE 파일을 읽을 수 없어요. 16kHz 모노 16비트 wav인지 확인해 주세요.")
            return
        }
        val chunker = Chunker(listener, chunkMillis)
        var pos = 0
        var next = System.nanoTime()
        try {
            while (running) {
                next += BLOCK_MS * 1_000_000L
                val wait = (next - System.nanoTime()) / 1_000_000L
                if (wait > 0) Thread.sleep(wait)
                if (isPaused) continue
                val block = ByteArray(BLOCK_BYTES)
                for (i in block.indices) block[i] = if (pos + i < pcm.size) pcm[pos + i] else 0
                pos += BLOCK_BYTES
                if (pos >= pcm.size + SAMPLE_RATE * 2) pos = 0 // 끝나면 1초 쉬고 처음부터
                chunker.add(block, block.size)
            }
        } catch (_: InterruptedException) {
        } finally {
            chunker.flush()
        }
    }

    /** 0.1초 블록을 모아 2초 조각을 만든다. */
    private class Chunker(private val listener: Listener, chunkMillis: Int) {
        private val chunkBytes = SAMPLE_RATE * 2 * chunkMillis / 1000
        private var buf = ByteArray(chunkBytes)
        private var filled = 0

        fun add(block: ByteArray, length: Int) {
            listener.onLevel(levelDb(block, length))
            var offset = 0
            while (offset < length) {
                val n = minOf(length - offset, chunkBytes - filled)
                System.arraycopy(block, offset, buf, filled, n)
                filled += n
                offset += n
                if (filled == chunkBytes) {
                    listener.onChunk(buf)
                    buf = ByteArray(chunkBytes)
                    filled = 0
                }
            }
        }

        fun flush() {
            if (filled >= SAMPLE_RATE * 2 / 5) listener.onChunk(buf.copyOf(filled)) // 0.2초 이상 남았을 때만
            filled = 0
        }
    }

    companion object {
        const val SAMPLE_RATE = 16_000
        const val CHUNK_MS = 2_000
        const val BLOCK_MS = 100
        private const val BLOCK_BYTES = SAMPLE_RATE * 2 * BLOCK_MS / 1000
        const val DEBUG_FILE = "debug_mic.wav"
        private const val TAG = "PcmRecorder"

        fun levelDb(pcm: ByteArray, length: Int): Float {
            var sum = 0.0
            val n = length / 2
            if (n == 0) return -90f
            for (i in 0 until n) {
                val s = ((pcm[2 * i + 1].toInt() shl 8) or (pcm[2 * i].toInt() and 0xFF)).toShort() / 32768.0
                sum += s * s
            }
            val rms = sqrt(sum / n)
            return if (rms <= 1e-5) -90f else (20 * log10(rms)).toFloat().coerceIn(-90f, 0f)
        }
    }
}
