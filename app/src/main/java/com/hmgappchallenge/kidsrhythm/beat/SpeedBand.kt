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

/** 비트 칩 글자. 예: "8비트" */
fun beatLabel(speedKmh: Float): String = "${speedZoneFor(speedKmh).beats}비트"

/** 속도 구간의 비트 (2·4·8·16비트). 리듬을 얼마나 잘게 쪼개는지이고, 박자는 항상 4/4다. 서버 `beatType` 값이다. */
fun beatTypeForSpeed(speedKmh: Float): Int = speedZoneFor(speedKmh).beats

/** BPM으로 비트를 고른다. S03에서 들려준 BPM과 같은 비트를 서버에 보내려고 쓴다 (docs/API.md "속도 → 비트 표"). */
fun beatTypeForBpm(bpm: Int): Int = speedZoneForBpm(bpm).beats

/** 완성곡 BPM(80 · 95 · 110 · 125)이 어느 구간인지. 사이 값은 가까운 구간으로 본다. */
fun speedZoneForBpm(bpm: Int): SpeedZone = when {
    bpm < 88 -> SpeedZone.STOP
    bpm < 103 -> SpeedZone.ALLEY
    bpm < 118 -> SpeedZone.CITY
    else -> SpeedZone.HIGHWAY
}
