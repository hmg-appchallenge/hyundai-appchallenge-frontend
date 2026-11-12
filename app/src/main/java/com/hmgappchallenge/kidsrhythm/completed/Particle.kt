package com.hmgappchallenge.kidsrhythm.completed

import kotlin.random.Random

/** 캐릭터 둘레로 퍼지는 꽃가루 모양. 음표와 작은 도형을 주황·노랑으로 섞는다. */
enum class ParticleShape { OrangeNote, YellowNote, OrangeDot, OrangeTile, YellowTile }

/**
 * 파티클 하나. 캐릭터 중심에서 [angleDegrees] 방향으로 [distanceDp]만큼 [durationMillis] 동안 퍼지며
 * [rotationDegrees]만큼 돌고 점점 투명해진다.
 */
data class Particle(
    val shape: ParticleShape,
    val bornAtMillis: Long,
    val angleDegrees: Float,
    val distanceDp: Float,
    val durationMillis: Long,
    val rotationDegrees: Float,
    val sizeDp: Float,
) {
    /** 0(태어남) ~ 1(사라짐). */
    fun progressAt(nowMillis: Long): Float = ((nowMillis - bornAtMillis).toFloat() / durationMillis).coerceIn(0f, 1f)

    fun isAlive(nowMillis: Long): Boolean = nowMillis - bornAtMillis < durationMillis
}

/** 디자인 주석의 파티클 값 범위. */
object ParticleSpec {
    val SpawnIntervalMillis = 300L..600L
    val DistanceDp = 40f..120f
    val DurationMillis = 1_500L..2_500L
    val RotationDegrees = -180f..180f
    val SizeDp = 8f..16f
}

fun randomParticle(random: Random, bornAtMillis: Long): Particle = Particle(
    shape = ParticleShape.entries[random.nextInt(ParticleShape.entries.size)],
    bornAtMillis = bornAtMillis,
    angleDegrees = random.nextFloat() * 360f,
    distanceDp = random.between(ParticleSpec.DistanceDp),
    durationMillis = random.nextLong(ParticleSpec.DurationMillis.first, ParticleSpec.DurationMillis.last + 1),
    rotationDegrees = random.between(ParticleSpec.RotationDegrees),
    sizeDp = random.between(ParticleSpec.SizeDp),
)

/** 다음 파티클이 생길 때까지 기다리는 시간. */
fun nextSpawnDelay(random: Random): Long =
    random.nextLong(ParticleSpec.SpawnIntervalMillis.first, ParticleSpec.SpawnIntervalMillis.last + 1)

private fun Random.between(range: ClosedFloatingPointRange<Float>): Float =
    range.start + nextFloat() * (range.endInclusive - range.start)
