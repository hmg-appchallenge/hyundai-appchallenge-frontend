package com.hmgappchallenge.kidsrhythm.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.beat.bpmForSpeed

/**
 * S03~S05에서 계속 도는 베이스 비트. 속도 구간마다 2마디짜리 소리 파일을 끊김 없이 반복한다.
 * 소리 파일은 노래 엔진(ai/songmaker/app_sounds.py)이 완성곡과 같은 소리로 만든다.
 */
class BeatPlayer(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        .build()
    private val samples: Map<Int, Int> = mapOf(
        80 to pool.load(context, R.raw.beat_080_2, 1),
        95 to pool.load(context, R.raw.beat_095_4, 1),
        110 to pool.load(context, R.raw.beat_110_8, 1),
        125 to pool.load(context, R.raw.beat_125_16, 1),
    )
    private val loaded = mutableSetOf<Int>()
    private var wantedBpm = 0
    private var playingBpm = 0
    private var streamId = 0
    private var volume = DEFAULT_VOLUME

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loaded += sampleId
                if (wantedBpm != 0 && playingBpm != wantedBpm && samples[wantedBpm] == sampleId) start(wantedBpm)
            }
        }
    }

    /** 이 속도에 맞는 비트를 튼다. 이미 같은 비트가 돌고 있으면 그대로 둔다. */
    fun playForSpeed(speedKmh: Float) = play(bpmForSpeed(speedKmh))

    fun play(bpm: Int) {
        val key = samples.keys.minByOrNull { kotlin.math.abs(it - bpm) } ?: return
        wantedBpm = key
        if (playingBpm == key) return
        start(key)
    }

    private fun start(bpm: Int) {
        val sample = samples[bpm] ?: return
        if (sample !in loaded) return
        if (streamId != 0) pool.stop(streamId)
        streamId = pool.play(sample, volume, volume, 1, -1, 1f)
        playingBpm = bpm
    }

    /** 녹음하는 동안에는 작게 (비트가 마이크로 들어가지 않게). */
    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
        if (streamId != 0) pool.setVolume(streamId, volume, volume)
    }

    fun stop() {
        if (streamId != 0) pool.stop(streamId)
        streamId = 0
        playingBpm = 0
        wantedBpm = 0
    }

    fun release() {
        stop()
        pool.release()
    }

    companion object {
        const val DEFAULT_VOLUME = 0.6f
        const val RECORDING_VOLUME = 0.2f
    }
}
