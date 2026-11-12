package com.hmgappchallenge.kidsrhythm.ui.component

import com.hmgappchallenge.kidsrhythm.server.LyricLine
import org.junit.Assert.assertEquals
import org.junit.Test

class SongPartsTest {
    private fun line(text: String, startMs: Long) = LyricLine(text, startMs, startMs + 2_000, "verse", emptyList())

    private val lines = listOf(line("하나", 1_000), line("둘", 3_000), line("셋", 5_000))

    @Test
    fun `첫 줄을 부르기 전에는 지금 부르는 줄이 없다`() {
        assertEquals(-1, currentLyricIndex(lines, 0))
        assertEquals(-1, currentLyricIndex(lines, 999))
        assertEquals(-1, currentLyricIndex(emptyList(), 10_000))
    }

    @Test
    fun `재생 위치를 지난 마지막 줄이 지금 부르는 줄이다`() {
        assertEquals(0, currentLyricIndex(lines, 1_000))
        assertEquals(0, currentLyricIndex(lines, 2_999))
        assertEquals(1, currentLyricIndex(lines, 3_000))
        assertEquals(2, currentLyricIndex(lines, 60_000))
    }

    @Test
    fun `재생 시간은 분과 두 자리 초로 보여 준다`() {
        assertEquals("0:00", formatClock(0))
        assertEquals("0:59", formatClock(59_999))
        assertEquals("1:42", formatClock(102_000))
        assertEquals("10:05", formatClock(605_000))
        assertEquals("0:00", formatClock(-500))
    }

    @Test
    fun `받침이 있으면 앞 조사, 없으면 뒤 조사를 붙인다`() {
        assertEquals("곰이야", "곰".josa("이야", "야"))
        assertEquals("콩콩이야", "콩콩이".josa("이야", "야"))
        assertEquals("뭉게가", "뭉게".josa("이", "가"))
        assertEquals("반짝이가", "반짝이".josa("이", "가"))
    }

    @Test
    fun `한글이 아니거나 빈 글자면 받침 없는 조사를 쓴다`() {
        assertEquals("Pleos가", "Pleos".josa("이", "가"))
        assertEquals("", "".josa("이", "가"))
    }
}
