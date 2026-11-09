package com.hmgappchallenge.kidsrhythm.recording

import com.hmgappchallenge.kidsrhythm.ui.component.Instrument

/** 녹음에 함께하는 가족 한 명과 그 사람이 고른 악기. */
data class FamilyMember(val id: Long, val name: String, val instrument: Instrument)

/** 녹음 화면에 그릴 순간 상태. */
data class RecordingSnapshot(
    val elapsedMillis: Long,
    /** 지금까지 쌓인 파형 막대 높이(dp). 오래된 것부터. */
    val levelHeights: List<Int>,
    val playingMemberIds: Set<Long>,
    val heardWords: List<String>,
)

/**
 * 녹음·음성 인식이 연결되기 전까지 화면을 움직이는 가짜 데이터.
 * 경과 시간만으로 모든 값을 정해서 같은 시간이면 항상 같은 화면이 나온다.
 */
object FakeRecording {
    // TODO(가족 목소리): S02-1에서 등록한 가족과 악기로 바꾼다
    val family = listOf(
        FamilyMember(1, "엄마", Instrument.DRUM),
        FamilyMember(2, "첫째", Instrument.SHAKER),
        FamilyMember(3, "둘째", Instrument.TAMBOURINE),
    )

    const val BAR_INTERVAL_MILLIS = 100L
    private const val SPEAKER_INTERVAL_MILLIS = 2_000L
    private const val WORD_INTERVAL_MILLIS = 3_000L
    private const val MAX_BARS = 200

    /** 시안의 파형 막대 높이 반복 패턴. */
    private val LevelPattern = listOf(30, 56, 20, 64, 84, 44)
    private val SampleWords = listOf("엄마", "둥둥", "랄랄라", "사과", "신나")

    /** 2초마다 바뀌는 "지금 소리 낸 사람" 순서. */
    private val SpeakerRotation = listOf(setOf(1L, 2L), setOf(2L), setOf(3L), setOf(1L, 3L), emptySet())

    fun snapshot(elapsedMillis: Long): RecordingSnapshot {
        // TODO(오디오): VoiceInput 마이크 입력의 음량으로 파형을 만든다
        val barCount = (elapsedMillis / BAR_INTERVAL_MILLIS).toInt()
        // 화면에는 최근 막대만 보이므로 오래된 것은 버린다
        val firstBar = (barCount - MAX_BARS).coerceAtLeast(0)
        // TODO(AI 서버): 화자 구분 결과로 지금 소리 낸 가족을 정한다
        val speakers = SpeakerRotation[((elapsedMillis / SPEAKER_INTERVAL_MILLIS) % SpeakerRotation.size).toInt()]
        // TODO(AI 서버 · Pleos Gleo AI): 음성 인식으로 들린 단어를 받는다
        val wordCount = (elapsedMillis / WORD_INTERVAL_MILLIS).toInt().coerceAtMost(SampleWords.size)
        return RecordingSnapshot(
            elapsedMillis = elapsedMillis,
            levelHeights = (firstBar until barCount).map { LevelPattern[it % LevelPattern.size] },
            playingMemberIds = speakers,
            heardWords = SampleWords.take(wordCount),
        )
    }
}

/** 경과 시간을 "01:24" 형식으로. */
fun formatElapsed(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis / 1_000
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
