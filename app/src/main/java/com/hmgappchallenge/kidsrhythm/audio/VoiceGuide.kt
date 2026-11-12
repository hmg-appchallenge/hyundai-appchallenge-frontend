package com.hmgappchallenge.kidsrhythm.audio

import android.content.Context
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * 짧은 음성 안내 (S02 "음악 한번 만들어볼까?").
 * 기기의 음성 합성(TTS)을 쓰고, 한국어 음성이 없으면 조용히 넘어간다. 화면 흐름은 음성을 기다리지 않는다.
 */
class VoiceGuide(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var pending: String? = null
    private var pendingAt = 0L

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            Log.w(TAG, "음성 합성을 쓸 수 없어요 ($status)")
            return
        }
        val result = tts.setLanguage(Locale.KOREAN)
        ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        if (!ready) {
            Log.w(TAG, "한국어 음성이 없어서 음성 안내를 건너뛰어요")
            return
        }
        // 아이들 앱이라 조금 밝고 또박또박하게
        tts.setPitch(1.15f)
        tts.setSpeechRate(0.95f)
        val text = pending
        pending = null
        // 엔진 준비가 늦어 화면이 이미 넘어갔으면 늦은 안내는 하지 않는다
        if (text != null && SystemClock.elapsedRealtime() - pendingAt < MAX_DELAY_MS) speak(text)
    }

    /** 바로 말한다. 엔진이 아직 준비 중이면 준비되는 대로 말한다. */
    fun speak(text: String) {
        if (!ready) {
            pending = text
            pendingAt = SystemClock.elapsedRealtime()
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun stop() {
        pending = null
        if (ready) tts.stop()
    }

    fun release() {
        tts.shutdown()
    }

    private companion object {
        const val TAG = "VoiceGuide"
        const val UTTERANCE_ID = "guide"
        const val MAX_DELAY_MS = 1_500L
    }
}
