package com.hmgappchallenge.kidsrhythm.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hmgappchallenge.kidsrhythm.audio.BeatPlayer

/** 노래를 재생할 때(S06) 처음 보이는 화면. */
enum class SongViewMode { Basic, Enlarged }

/** 녹음하는 동안 스피커로 나오는 비트 크기 (S08-2). */
enum class RecordingBeatLevel(val label: String, val volume: Float) {
    Small("작게", BeatPlayer.RECORDING_VOLUME),
    Normal("보통", BeatPlayer.DEFAULT_VOLUME),
    Off("끄기", 0f),
}

/** S06에서 처음 크게 보기로 열지. 기본이 크게 보기이거나, 정차 중 자동 크게 보기가 켜져 있고 정차 중이면 크게 연다. */
fun startsEnlarged(mode: SongViewMode, autoEnlargeWhenStopped: Boolean, stopped: Boolean): Boolean =
    mode == SongViewMode.Enlarged || (autoEnlargeWhenStopped && stopped)

/** S08 설정 값. 기기에 남기고, 화면이 바로 다시 그리도록 Compose 상태로도 들고 있다. */
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var songViewMode by mutableStateOf(enumOr(prefs.getString(KEY_VIEW, null), SongViewMode.Basic))
        private set
    var autoEnlargeWhenStopped by mutableStateOf(prefs.getBoolean(KEY_AUTO_ENLARGE, true))
        private set
    var voiceGuideEnabled by mutableStateOf(prefs.getBoolean(KEY_VOICE_GUIDE, true))
        private set
    var recordingBeat by mutableStateOf(enumOr(prefs.getString(KEY_BEAT, null), RecordingBeatLevel.Small))
        private set

    fun updateSongViewMode(value: SongViewMode) {
        songViewMode = value
        prefs.edit().putString(KEY_VIEW, value.name).apply()
    }

    fun updateAutoEnlargeWhenStopped(value: Boolean) {
        autoEnlargeWhenStopped = value
        prefs.edit().putBoolean(KEY_AUTO_ENLARGE, value).apply()
    }

    fun updateVoiceGuideEnabled(value: Boolean) {
        voiceGuideEnabled = value
        prefs.edit().putBoolean(KEY_VOICE_GUIDE, value).apply()
    }

    fun updateRecordingBeat(value: RecordingBeatLevel) {
        recordingBeat = value
        prefs.edit().putString(KEY_BEAT, value.name).apply()
    }

    private companion object {
        const val KEY_VIEW = "song_view_mode"
        const val KEY_AUTO_ENLARGE = "auto_enlarge_when_stopped"
        const val KEY_VOICE_GUIDE = "voice_guide"
        const val KEY_BEAT = "recording_beat"

        inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
            enumValues<T>().firstOrNull { it.name == name } ?: fallback
    }
}
