package com.hmgappchallenge.kidsrhythm.completed

import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.server.GeneratedSong
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** S06에 보여 줄 완성곡. [highlightWord]는 가사에서 주황으로 강조할 단어다. */
data class CompletedSong(
    val title: String,
    val highlightWord: String?,
    val lyrics: List<String>,
    val durationMillis: Long,
    val createdAtMillis: Long,
    val bpm: Int,
)

/**
 * 생성 결과로 완성곡을 만든다. 제목은 서버 제목 → "단어 노래" → "M월 d일 노래" 순으로 정한다.
 * [heardWord]는 녹음에서 들린 단어로, 서버 단어보다 먼저 쓴다.
 */
fun completedSongOf(
    generated: GeneratedSong,
    heardWord: String?,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): CompletedSong {
    val word = heardWord?.takeIf { it.isNotBlank() } ?: generated.words.firstOrNull()
    val title = generated.title ?: word?.let { "$it 노래" } ?: dateTitle(nowMillis, zone)
    return CompletedSong(
        title = title,
        highlightWord = word,
        lyrics = generated.lyrics,
        durationMillis = TimeUnit.SECONDS.toMillis(generated.durationSec.toLong()),
        createdAtMillis = nowMillis,
        bpm = generated.bpm,
    )
}

/** 들린 단어가 없을 때 붙는 제목. 예: "11월 11일 노래" */
internal fun dateTitle(nowMillis: Long, zone: ZoneId): String {
    val date = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    return "${date.monthValue}월 ${date.dayOfMonth}일 노래"
}

/** 지금 부르는 가사 줄. 곡 길이를 줄 수로 똑같이 나눈다. */
fun lyricIndexAt(elapsedMillis: Long, durationMillis: Long, lineCount: Int): Int {
    if (lineCount <= 0 || durationMillis <= 0) return 0
    val index = (elapsedMillis.coerceIn(0, durationMillis) * lineCount / durationMillis).toInt()
    return index.coerceAtMost(lineCount - 1)
}

/** 재생 시간 "0:39" 형식. */
fun formatPlayTime(millis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis.coerceAtLeast(0))
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

/** 미니 플레이어 부제. 예: "엄마 북 · 첫째 탬버린" */
fun memberInstrumentsLabel(members: List<FamilyMember>): String =
    members.joinToString(" · ") { "${it.name} ${it.instrument.label}" }
