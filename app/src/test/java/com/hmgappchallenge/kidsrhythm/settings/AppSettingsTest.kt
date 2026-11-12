package com.hmgappchallenge.kidsrhythm.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {
    @Test
    fun `기본 보기가 크게 보기면 언제나 크게 연다`() {
        assertTrue(startsEnlarged(SongViewMode.Enlarged, autoEnlargeWhenStopped = false, stopped = false))
    }

    @Test
    fun `정차하면 크게 보기가 켜져 있으면 정차 중에만 크게 연다`() {
        assertTrue(startsEnlarged(SongViewMode.Basic, autoEnlargeWhenStopped = true, stopped = true))
        assertFalse(startsEnlarged(SongViewMode.Basic, autoEnlargeWhenStopped = true, stopped = false))
        assertFalse(startsEnlarged(SongViewMode.Basic, autoEnlargeWhenStopped = false, stopped = true))
    }
}
