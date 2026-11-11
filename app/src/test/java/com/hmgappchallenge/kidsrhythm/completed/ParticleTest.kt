package com.hmgappchallenge.kidsrhythm.completed

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParticleTest {
    @Test
    fun `파티클 값은 디자인 주석 범위 안에서 정해진다`() {
        val random = Random(7)
        repeat(500) {
            val p = randomParticle(random, bornAtMillis = 0)
            assertTrue(p.angleDegrees in 0f..360f)
            assertTrue(p.distanceDp in ParticleSpec.DistanceDp)
            assertTrue(p.durationMillis in ParticleSpec.DurationMillis)
            assertTrue(p.rotationDegrees in ParticleSpec.RotationDegrees)
            assertTrue(p.sizeDp in ParticleSpec.SizeDp)
            assertTrue(nextSpawnDelay(random) in ParticleSpec.SpawnIntervalMillis)
        }
    }

    @Test
    fun `파티클은 시간이 지나면 끝까지 퍼지고 사라진다`() {
        val p = Particle(ParticleShape.OrangeDot, 1_000, 0f, 100f, 2_000, 90f, 10f)
        assertEquals(0f, p.progressAt(1_000))
        assertEquals(0.5f, p.progressAt(2_000))
        assertTrue(p.isAlive(2_999))
        assertFalse(p.isAlive(3_000))
    }
}
