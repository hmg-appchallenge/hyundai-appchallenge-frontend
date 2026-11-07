package com.hmgappchallenge.kidsrhythm.beat

/** 기획서 07번 매핑 초안. 0 / 1~30 / 31~70 / 71+ km/h → BPM 80 / 95 / 110 / 125. */
fun bpmForSpeed(speedKmh: Float): Int = when {
    speedKmh <= 0f -> 80
    speedKmh <= 30f -> 95
    speedKmh <= 70f -> 110
    else -> 125
}
