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

    @Test
    fun `속도 구간마다 비트가 2·4·8·16비트로 나뉜다`() {
        assertEquals(2, beatTypeForSpeed(0f))
        assertEquals(4, beatTypeForSpeed(1f))
        assertEquals(4, beatTypeForSpeed(30f))
        assertEquals(8, beatTypeForSpeed(31f))
        assertEquals(8, beatTypeForSpeed(70f))
        assertEquals(16, beatTypeForSpeed(71f))
    }

    @Test
    fun `BPM으로 고른 비트는 같은 속도에서 고른 비트와 같다`() {
        // 서버에는 S03에서 들려준 BPM으로 비트를 보내므로 두 함수가 어긋나면 안 된다
        listOf(0f, 1f, 15f, 30f, 31f, 42f, 70f, 71f, 120f).forEach { speed ->
            assertEquals("speed=$speed", beatTypeForSpeed(speed), beatTypeForBpm(bpmForSpeed(speed)))
        }
    }
}
