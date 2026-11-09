package com.hmgappchallenge.kidsrhythm.archive

import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class SongFormatTest {
    private val zone = ZoneId.of("Asia/Seoul")
    private fun millis(text: String) = LocalDateTime.parse(text).atZone(zone).toInstant().toEpochMilli()
    private val now = millis("2026-11-09T10:00:00")

    @Test
    fun `만든 날짜는 오늘·어제·월일로 보여 준다`() {
        assertEquals("오늘", dayLabel(millis("2026-11-09T00:10:00"), now, zone))
        assertEquals("어제", dayLabel(millis("2026-11-08T23:59:00"), now, zone))
        assertEquals("11월 7일", dayLabel(millis("2026-11-07T12:00:00"), now, zone))
    }

    @Test
    fun `곡 길이는 분과 초로 보여 준다`() {
        assertEquals("1분 42초", durationLabel(102_000))
        assertEquals("42초", durationLabel(42_000))
    }

    @Test
    fun `최근 곡 부가 정보는 날짜와 길이를 점으로 잇는다`() {
        val song = ArchivedSong(1, "사과 노래", millis("2026-11-08T15:00:00"), 102_000, emptyList(), "")
        assertEquals("어제 · 1분 42초", songMetaLabel(song, now, zone))
    }
}
