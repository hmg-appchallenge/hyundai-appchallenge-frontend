package com.hmgappchallenge.kidsrhythm.beat

/**
 * 속도 구간 이름. [bpmForSpeed]와 같은 경계를 쓴다.
 * TODO(기획): 구간 이름과 비트 수 기준이 정해지면 바꾼다. 시안은 42 km/h를 "고속 · 4비트"로 적었다
 */
fun speedBandLabel(speedKmh: Float): String = when {
    speedKmh <= 0f -> "정차"
    speedKmh <= 30f -> "저속"
    speedKmh <= 70f -> "중속"
    else -> "고속"
}

/** 비트 수. 기준이 없어 시안 값으로 고정한다. */
const val BEATS_PER_BAR = 4
