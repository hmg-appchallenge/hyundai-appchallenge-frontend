package com.hmgappchallenge.kidsrhythm.completed

import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.server.HeardWord
import com.hmgappchallenge.kidsrhythm.server.LyricLine
import com.hmgappchallenge.kidsrhythm.server.SongResult
import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class CompletedSongTest {
    private val zone: ZoneId = ZoneOffset.UTC
    private val nov11 = LocalDate.of(2026, 11, 11).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun result(title: String = "", words: List<String> = emptyList()) = SongResult(
        title = title, durationMs = 102_000, bpm = 110, beatType = 8, firstBeatMs = 0,
        lyrics = listOf(0L, 10_000L, 40_000L).mapIndexed { i, start -> LyricLine("줄$i", start, start + 10_000, "verse", emptyList()) },
        words = words.map { HeardWord(it, null, "word") }, onomatopoeia = emptyList(), members = emptyList(), audioUrl = "",
    )

    @Test
    fun `서버 제목이 있으면 그대로 쓰고 첫 단어를 강조한다`() {
        val song = completedSongOf(result(title = "빨간 사과 노래", words = listOf("사과", "엄마")), nov11, zone)
        assertEquals("빨간 사과 노래", song.title)
        assertEquals("사과", song.highlightWord)
    }

    @Test
    fun `서버 제목이 비어 오면 첫 단어로 단어 노래 제목을 짓는다`() {
        assertEquals("사과 노래", completedSongOf(result(words = listOf("사과")), nov11, zone).title)
    }

    @Test
    fun `들린 단어도 없으면 날짜 제목을 붙인다`() {
        val song = completedSongOf(result(), nov11, zone)
        assertEquals("11월 11일 드라이브 노래", song.title)
        assertEquals(null, song.highlightWord)
    }

    @Test
    fun `가사 줄은 서버가 준 시작 시간에 맞춰 넘어간다`() {
        val song = completedSongOf(result(), nov11, zone)
        assertEquals(listOf(0, 0, 1, 1, 2), listOf(0L, 9_999L, 10_000L, 39_999L, 90_000L).map { lyricIndexAt(song, it) })
    }

    @Test
    fun `가사 줄은 곡 길이를 줄 수로 나눠 넘어간다`() {
        assertEquals(0, lyricIndexAt(0, 100_000, 4))
        assertEquals(1, lyricIndexAt(25_000, 100_000, 4))
        assertEquals(3, lyricIndexAt(100_000, 100_000, 4))
        assertEquals(0, lyricIndexAt(5_000, 100_000, 0))
    }

    @Test
    fun `재생 시간은 분과 두 자리 초로 보여 준다`() {
        assertEquals("0:39", formatPlayTime(39_000))
        assertEquals("1:42", formatPlayTime(102_000))
    }

    @Test
    fun `미니 플레이어 부제는 가족 이름과 악기를 잇는다`() {
        val members = listOf(FamilyMember(1, "엄마", Instrument.DRUM), FamilyMember(2, "첫째", Instrument.TAMBOURINE))
        assertEquals("엄마 북 · 첫째 탬버린", memberInstrumentsLabel(members))
    }
}
