package com.hmgappchallenge.kidsrhythm.beat

/**
 * 속도 구간. [bpmForSpeed]와 같은 경계(0 / 1~30 / 31~70 / 71+ km/h)를 쓴다.
 * [apiCode]는 노래 생성 요청의 speedZone 값이다.
 */
enum class SpeedZone(val label: String, val beats: Int, val apiCode: String) {
    STOP("정차", 2, "stop"),
    ALLEY("골목", 4, "alley"),
    CITY("시내", 8, "city"),
    HIGHWAY("고속", 16, "highway"),
}

fun speedZoneFor(speedKmh: Float): SpeedZone = when {
    speedKmh <= 0f -> SpeedZone.STOP
    speedKmh <= 30f -> SpeedZone.ALLEY
    speedKmh <= 70f -> SpeedZone.CITY
    else -> SpeedZone.HIGHWAY
}

/** 속도 구간 칩 글자. 예: "시내 · 8비트" */
fun speedZoneLabel(speedKmh: Float): String = speedZoneFor(speedKmh).let { "${it.label} · ${it.beats}비트" }

/** 완성곡 BPM(80 · 95 · 110 · 125)이 어느 구간인지. 사이 값은 가까운 구간으로 본다. */
fun speedZoneForBpm(bpm: Int): SpeedZone = when {
    bpm < 88 -> SpeedZone.STOP
    bpm < 103 -> SpeedZone.ALLEY
    bpm < 118 -> SpeedZone.CITY
    else -> SpeedZone.HIGHWAY
}
