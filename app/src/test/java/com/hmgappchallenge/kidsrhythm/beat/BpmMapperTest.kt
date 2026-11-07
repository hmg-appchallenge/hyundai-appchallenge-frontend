package com.hmgappchallenge.kidsrhythm.beat

import org.junit.Assert.assertEquals
import org.junit.Test

class BpmMapperTest {
    @Test
    fun `정차 중이면 BPM 80이다`() {
        assertEquals(80, bpmForSpeed(0f))
    }

    @Test
    fun `속도 구간 경계값이 기획서 구간에 맞는 BPM으로 변환된다`() {
        assertEquals(95, bpmForSpeed(1f))
        assertEquals(95, bpmForSpeed(30f))
        assertEquals(110, bpmForSpeed(31f))
        assertEquals(110, bpmForSpeed(70f))
        assertEquals(125, bpmForSpeed(71f))
    }
}
