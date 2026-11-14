package com.hmgappchallenge.kidsrhythm.beat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BeatClockTest {
    @Test
    fun `첫 박 전이나 BPM을 모르면 박이 없다`() {
        assertNull(beatPositionAt(positionMs = 400, bpm = 120, firstBeatMs = 500))
        assertNull(beatPositionAt(positionMs = 1_000, bpm = 0, firstBeatMs = 0))
    }

    @Test
    fun `120 BPM이면 0_5초마다 다음 박이다`() {
        assertEquals(BeatPosition(0, 0f), beatPositionAt(500, 120, 500))
        val third = beatPositionAt(500 + 1_250, 120, 500)!!
        assertEquals(2L, third.index)
        assertEquals(0.5f, third.phase, 0.001f)
    }

    @Test
    fun `박이 온 순간은 눌리고 박 가운데에서 가장 높이 뜬다`() {
        val hit = bounceAt(0f)
        assertEquals(0f, hit.lift, 0.001f)
        assertTrue(hit.squash < 1f)
        val middle = bounceAt(0.5f)
        assertEquals(1f, middle.lift, 0.001f)
        assertEquals(1f, middle.squash, 0.001f)
    }
}
