package com.hmgappchallenge.kidsrhythm.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.component.Instrument

/** 악기 한 번 치는 소리(S04 "소리 낸 사람의 악기가 울려요")와 3초 미리 듣기(멤버 추가). */
class InstrumentSounds(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        .build()
    private val hits: Map<Instrument, Int> = Instrument.entries.associateWith { pool.load(context, hitRes(it), 1) }
    private val previews: Map<Instrument, Int> = Instrument.entries.associateWith { pool.load(context, previewRes(it), 1) }
    private var previewStream = 0

    fun hit(instrument: Instrument, volume: Float = 0.8f) {
        hits[instrument]?.let { pool.play(it, volume, volume, 1, 0, 1f) }
    }

    fun preview(instrument: Instrument) {
        if (previewStream != 0) pool.stop(previewStream)
        previewStream = previews[instrument]?.let { pool.play(it, 0.9f, 0.9f, 2, 0, 1f) } ?: 0
    }

    fun stopPreview() {
        if (previewStream != 0) pool.stop(previewStream)
        previewStream = 0
    }

    fun release() = pool.release()

    private companion object {
        fun hitRes(i: Instrument): Int = when (i) {
            Instrument.DRUM -> R.raw.inst_drum_hit
            Instrument.TAMBOURINE -> R.raw.inst_tambourine_hit
            Instrument.SHAKER -> R.raw.inst_shaker_hit
            Instrument.BELL -> R.raw.inst_bell_hit
            Instrument.UKULELE -> R.raw.inst_ukulele_hit
            Instrument.STEELDRUM -> R.raw.inst_steeldrum_hit
            Instrument.WOODBLOCK -> R.raw.inst_woodblock_hit
            Instrument.GUITAR -> R.raw.inst_guitar_hit
            Instrument.HARP -> R.raw.inst_harp_hit
            Instrument.XYLOPHONE -> R.raw.inst_xylophone_hit
            Instrument.PIANO -> R.raw.inst_piano_hit
            Instrument.MARACAS -> R.raw.inst_maracas_hit
        }

        fun previewRes(i: Instrument): Int = when (i) {
            Instrument.DRUM -> R.raw.inst_drum_preview
            Instrument.TAMBOURINE -> R.raw.inst_tambourine_preview
            Instrument.SHAKER -> R.raw.inst_shaker_preview
            Instrument.BELL -> R.raw.inst_bell_preview
            Instrument.UKULELE -> R.raw.inst_ukulele_preview
            Instrument.STEELDRUM -> R.raw.inst_steeldrum_preview
            Instrument.WOODBLOCK -> R.raw.inst_woodblock_preview
            Instrument.GUITAR -> R.raw.inst_guitar_preview
            Instrument.HARP -> R.raw.inst_harp_preview
            Instrument.XYLOPHONE -> R.raw.inst_xylophone_preview
            Instrument.PIANO -> R.raw.inst_piano_preview
            Instrument.MARACAS -> R.raw.inst_maracas_preview
        }
    }
}
