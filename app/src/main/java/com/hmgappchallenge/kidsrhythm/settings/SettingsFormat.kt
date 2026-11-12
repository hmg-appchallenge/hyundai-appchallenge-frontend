package com.hmgappchallenge.kidsrhythm.settings

import java.time.Instant
import java.time.ZoneId

private const val MEGABYTE = 1024L * 1024L

/** 저장 공간 글자. 1MB가 안 되면 "1MB 미만", 아니면 반올림한 MB. 예: "12MB" */
fun storageLabel(bytes: Long): String =
    if (bytes < MEGABYTE) "1MB 미만" else "${(bytes + MEGABYTE / 2) / MEGABYTE}MB"

/**
 * 보호자 동의 줄의 부제. 예: "동의함 · 2026.11.07 · 철회하면 녹음이 꺼져요"
 * 동의 날짜를 남기기 전에 동의한 경우에는 날짜 없이 보여 준다.
 */
fun consentLabel(agreed: Boolean, agreedAtMillis: Long?, zone: ZoneId = ZoneId.systemDefault()): String {
    if (!agreed) return "동의 전 · 노래를 만들 때 다시 물어봐요"
    if (agreedAtMillis == null) return "동의함 · 철회하면 녹음이 꺼져요"
    val d = Instant.ofEpochMilli(agreedAtMillis).atZone(zone).toLocalDate()
    return "동의함 · %d.%02d.%02d · 철회하면 녹음이 꺼져요".format(d.year, d.monthValue, d.dayOfMonth)
}
