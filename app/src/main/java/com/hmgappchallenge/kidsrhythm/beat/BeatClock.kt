package com.hmgappchallenge.kidsrhythm.beat

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

/** 노래 재생 위치가 몇 번째 박의 어디쯤인지. [phase]는 이 박 안에서 0(박이 온 순간)~1(다음 박 직전). */
data class BeatPosition(val index: Long, val phase: Float)

/** 재생 위치 [positionMs]가 몇 번째 박인지. BPM을 모르거나 첫 박([firstBeatMs]) 전이면 null. */
fun beatPositionAt(positionMs: Long, bpm: Int, firstBeatMs: Long): BeatPosition? {
    if (bpm <= 0 || positionMs < firstBeatMs) return null
    val beats = (positionMs - firstBeatMs) * bpm / 60_000.0
    val index = floor(beats).toLong()
    return BeatPosition(index, (beats - index).toFloat())
}

/** 박마다 튀는 모습. [lift]는 위로 뜬 정도(0~1), [squash]는 세로 비율(1보다 작으면 눌림). */
data class Bounce(val lift: Float, val squash: Float)

/** 박이 온 순간 살짝 눌렸다가, 박 가운데쯤 가장 높이 떴다가, 다음 박에 다시 내려앉는다. */
fun bounceAt(phase: Float): Bounce {
    val p = phase.coerceIn(0f, 1f)
    val lift = sin(PI * p).toFloat()
    val squash = if (p < SQUASH_PHASE) 1f - MAX_SQUASH * (1f - p / SQUASH_PHASE) else 1f
    return Bounce(lift, squash)
}

/** 박이 온 뒤 이만큼(박 길이 비율) 동안 눌렸다 펴진다. */
private const val SQUASH_PHASE = 0.18f

/** 가장 많이 눌렸을 때 줄어드는 세로 비율. */
private const val MAX_SQUASH = 0.06f
