package com.hmgappchallenge.kidsrhythm.beat

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedBandTest {
    @Test
    fun `속도 구간은 BPM 구간과 같은 경계로 정차·골목·시내·고속으로 나뉜다`() {
        assertEquals(SpeedZone.STOP, speedZoneFor(0f))
        assertEquals(SpeedZone.ALLEY, speedZoneFor(1f))
        assertEquals(SpeedZone.ALLEY, speedZoneFor(30f))
        assertEquals(SpeedZone.CITY, speedZoneFor(31f))
        assertEquals(SpeedZone.CITY, speedZoneFor(70f))
        assertEquals(SpeedZone.HIGHWAY, speedZoneFor(71f))
    }

    @Test
    fun `구간마다 비트 수는 2·4·8·16비트다`() {
        assertEquals(listOf(2, 4, 8, 16), SpeedZone.entries.map { it.beats })
        assertEquals(listOf("stop", "alley", "city", "highway"), SpeedZone.entries.map { it.apiCode })
    }

    @Test
    fun `속도 42 km_h는 110 BPM 시내 8비트다`() {
        assertEquals(110, bpmForSpeed(42f))
        assertEquals("시내 · 8비트", speedZoneLabel(42f))
    }
}
