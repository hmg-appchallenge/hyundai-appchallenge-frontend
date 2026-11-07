package com.hmgappchallenge.kidsrhythm.audio

import kotlinx.coroutines.flow.Flow

/** 음성 안내 출력. Pleos TextToSpeech SDK 사용 여부는 미정이다. */
interface SpeechOutput {
    suspend fun speak(text: String)
    fun stop()
}

/** 마이크 입력. 수집하는 동안 PCM 프레임을 내보낸다. 좌석별 입력 지원 여부는 미정이다. */
interface VoiceInput {
    val frames: Flow<ShortArray>
}
