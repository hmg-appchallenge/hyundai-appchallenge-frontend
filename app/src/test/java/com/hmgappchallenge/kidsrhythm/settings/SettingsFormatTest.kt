package com.hmgappchallenge.kidsrhythm.settings

import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsFormatTest {
    @Test
    fun `저장 공간은 MB로 반올림하고 1MB가 안 되면 1MB 미만이다`() {
        assertEquals("1MB 미만", storageLabel(500_000))
        assertEquals("12MB", storageLabel(12L * 1024 * 1024 + 100))
        assertEquals("2MB", storageLabel(1_600_000))
    }

    @Test
    fun `동의한 날짜를 점으로 이어 보여 주고 동의 전이면 다시 묻는다고 알린다`() {
        val nov7 = LocalDate.of(2026, 11, 7).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals("동의함 · 2026.11.07 · 철회하면 녹음이 꺼져요", consentLabel(true, nov7, ZoneOffset.UTC))
        assertEquals("동의 전 · 노래를 만들 때 다시 물어봐요", consentLabel(false, null, ZoneOffset.UTC))
    }

    @Test
    fun `날짜를 남기기 전에 동의했으면 날짜 없이 동의함으로 보여 준다`() {
        assertEquals("동의함 · 철회하면 녹음이 꺼져요", consentLabel(true, null, ZoneOffset.UTC))
    }
}
