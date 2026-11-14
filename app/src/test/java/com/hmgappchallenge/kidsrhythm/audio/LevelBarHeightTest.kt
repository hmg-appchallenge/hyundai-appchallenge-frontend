package com.hmgappchallenge.kidsrhythm.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelBarHeightTest {
    @Test
    fun `조용하면 가장 낮고 크게 말할수록 막대가 높아진다`() {
        assertEquals(8, levelBarHeight(-90f))
        assertEquals(8, levelBarHeight(-60f))
        assertEquals(45, levelBarHeight(-10f))
        assertEquals(45, levelBarHeight(0f))
        assertTrue(levelBarHeight(-30f) in 9..44)
    }
}
