package com.hmgappchallenge.kidsrhythm.beat

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedBandTest {
    @Test
    fun `속도 구간 이름은 BPM 구간과 같은 경계로 나뉜다`() {
        assertEquals("정차", speedBandLabel(0f))
        assertEquals("저속", speedBandLabel(1f))
        assertEquals("저속", speedBandLabel(30f))
        assertEquals("중속", speedBandLabel(31f))
        assertEquals("중속", speedBandLabel(70f))
        assertEquals("고속", speedBandLabel(71f))
    }

    @Test
    fun `시안 속도 42 km_h는 110 BPM 중속 구간이다`() {
        assertEquals(110, bpmForSpeed(42f))
        assertEquals("중속", speedBandLabel(42f))
    }
}
