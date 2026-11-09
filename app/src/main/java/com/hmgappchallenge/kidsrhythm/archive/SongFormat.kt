package com.hmgappchallenge.kidsrhythm.archive

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/** 곡 목록 부가 정보 ("어제 · 1분 42초"). */
fun songMetaLabel(song: ArchivedSong, nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    "${dayLabel(song.createdAtMillis, nowMillis, zone)} · ${durationLabel(song.durationMillis)}"

internal fun dayLabel(createdAtMillis: Long, nowMillis: Long, zone: ZoneId): String {
    val created = Instant.ofEpochMilli(createdAtMillis).atZone(zone).toLocalDate()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    return when (ChronoUnit.DAYS.between(created, today)) {
        0L -> "오늘"
        1L -> "어제"
        else -> monthDay(created)
    }
}

private fun monthDay(date: LocalDate) = "${date.monthValue}월 ${date.dayOfMonth}일"

internal fun durationLabel(durationMillis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes == 0L) "${seconds}초" else "${minutes}분 ${seconds}초"
}
