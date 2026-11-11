package com.hmgappchallenge.kidsrhythm.beat

/**
 * 속도 구간 이름. [bpmForSpeed]와 같은 경계를 쓴다.
 * 피그마 스피드 칩 메모: 0 / 1~30 / 31~70 / 71+ km/h → 2 / 4 / 8 / 16비트.
 */
fun speedBandLabel(speedKmh: Float): String = when {
    speedKmh <= 0f -> "정차"
    speedKmh <= 30f -> "저속"
    speedKmh <= 70f -> "중속"
    else -> "고속"
}

/** 속도 구간의 비트 (2·4·8·16비트). 리듬을 얼마나 잘게 쪼개는지이고, 박자는 항상 4/4다. */
fun beatTypeForSpeed(speedKmh: Float): Int = when {
    speedKmh <= 0f -> 2
    speedKmh <= 30f -> 4
    speedKmh <= 70f -> 8
    else -> 16
}

/** BPM으로 비트를 고른다 (서버에 보낼 때 S03과 같은 값을 쓰려고). */
fun beatTypeForBpm(bpm: Int): Int = when {
    bpm <= 80 -> 2
    bpm <= 95 -> 4
    bpm <= 110 -> 8
    else -> 16
}
