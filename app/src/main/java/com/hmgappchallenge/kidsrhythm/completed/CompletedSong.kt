package com.hmgappchallenge.kidsrhythm.completed

import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.server.SongResult
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * S06에 보여 줄 완성곡. [highlightWord]는 가사에서 주황으로 강조할 단어다.
 * [lyricStartsMillis]는 줄마다 부르기 시작하는 시간이고, 비어 있으면 곡 길이를 줄 수로 나눈다.
 */
data class CompletedSong(
    val title: String,
    val highlightWord: String?,
    val lyrics: List<String>,
    val durationMillis: Long,
    val createdAtMillis: Long,
    val bpm: Int,
    val lyricStartsMillis: List<Long> = emptyList(),
    val lyricSections: List<String> = emptyList(),
)

/**
 * 서버 결과로 완성곡을 만든다. 제목은 서버 제목(AI 실패 시 서버가 "첫 단어 + 노래"로 짓는다)을 쓰고,
 * 비어 오면 "첫 단어 노래" → "M월 d일 노래" 순으로 정한다.
 */
fun completedSongOf(
    result: SongResult,
    createdAtMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): CompletedSong {
    val word = result.words.firstOrNull()?.text?.takeIf { it.isNotBlank() }
    val title = result.title.takeIf { it.isNotBlank() } ?: word?.let { "$it 노래" } ?: dateTitle(createdAtMillis, zone)
    return CompletedSong(
        title = title,
        highlightWord = word,
        lyrics = result.lyrics.map { it.text },
        durationMillis = result.durationMs,
        createdAtMillis = createdAtMillis,
        bpm = result.bpm,
        lyricStartsMillis = result.lyrics.map { it.startMs },
        lyricSections = result.lyrics.map { it.section },
    )
}

/** 들린 단어가 없을 때 붙는 제목. 예: "11월 11일 노래" */
internal fun dateTitle(nowMillis: Long, zone: ZoneId): String {
    val date = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    return "${date.monthValue}월 ${date.dayOfMonth}일 노래"
}

/** 지금 부르는 가사 줄. 줄마다 시작 시간이 있으면 그것으로, 없으면 곡 길이를 줄 수로 똑같이 나눠 고른다. */
fun lyricIndexAt(song: CompletedSong, elapsedMillis: Long): Int {
    if (song.lyricStartsMillis.size == song.lyrics.size && song.lyrics.isNotEmpty()) {
        return song.lyricStartsMillis.indexOfLast { it <= elapsedMillis }.coerceAtLeast(0)
    }
    return lyricIndexAt(elapsedMillis, song.durationMillis, song.lyrics.size)
}

/** 곡 길이를 줄 수로 똑같이 나눠 지금 줄을 고른다. */
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
