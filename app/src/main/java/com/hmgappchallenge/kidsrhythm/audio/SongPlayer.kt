package com.hmgappchallenge.kidsrhythm.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 완성곡·보관함 노래 재생. 화면은 [positionMs]를 읽어 가사를 강조하고 진행 막대를 그린다.
 * 재생 중에는 화면이 [tick]을 프레임마다 불러 위치를 갱신한다.
 */
class SongPlayer {
    private var player: MediaPlayer? = null

    var path: String? by mutableStateOf(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var positionMs by mutableLongStateOf(0L)
        private set
    var durationMs by mutableLongStateOf(0L)
        private set

    /** 파일을 연다. 같은 파일이면 그대로 둔다. */
    fun load(file: String, autoplay: Boolean = true, knownDurationMs: Long = 0L) {
        if (file == path && player != null) {
            if (autoplay) play()
            return
        }
        release()
        val mp = MediaPlayer()
        try {
            mp.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            mp.setDataSource(file)
            mp.prepare()
        } catch (e: Exception) {
            Log.w(TAG, "노래 파일을 열 수 없어요: $file", e)
            mp.release()
            return
        }
        mp.setOnCompletionListener {
            isPlaying = false
            positionMs = durationMs
        }
        player = mp
        path = file
        durationMs = mp.duration.toLong().takeIf { it > 0 } ?: knownDurationMs
        positionMs = 0L
        if (autoplay) play()
    }

    fun play() {
        val mp = player ?: return
        if (positionMs >= durationMs && durationMs > 0) mp.seekTo(0)
        mp.start()
        isPlaying = true
    }

    fun pause() {
        player?.takeIf { it.isPlaying }?.pause()
        isPlaying = false
        tick()
    }

    fun toggle() = if (isPlaying) pause() else play()

    fun seekTo(ms: Long) {
        val mp = player ?: return
        mp.seekTo(ms.coerceIn(0L, durationMs).toInt())
        positionMs = ms.coerceIn(0L, durationMs)
        if (!isPlaying) play()
    }

    /** 재생 위치를 읽어 둔다. */
    fun tick() {
        val mp = player ?: return
        try {
            positionMs = mp.currentPosition.toLong()
        } catch (_: IllegalStateException) {
        }
    }

    fun release() {
        player?.release()
        player = null
        path = null
        isPlaying = false
        positionMs = 0L
        durationMs = 0L
    }

    private companion object {
        const val TAG = "SongPlayer"
    }
}
