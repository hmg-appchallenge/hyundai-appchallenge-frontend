package com.hmgappchallenge.kidsrhythm.server

import java.io.File
import kotlinx.coroutines.delay

/** 요청 형식은 임시다. AI 서버 담당자와 API를 맞추면 수정한다. */
data class SongRequest(
    val recording: File,
    val bpm: Int,
)

/** 기획서 07번의 응답 JSON 초안. */
data class GeneratedSong(
    val words: List<String>,
    val onomatopoeia: List<String>,
    val seatZone: String?,
    val bpm: Int,
    val lyrics: List<String>,
    val audioUrl: String,
)

interface SongGenerator {
    suspend fun generate(request: SongRequest): GeneratedSong
}

/** AI 서버 연동 전까지 쓰는 목(mock) 구현. */
class FakeSongGenerator(
    private val latencyMillis: Long = 2_000L,
) : SongGenerator {
    override suspend fun generate(request: SongRequest): GeneratedSong {
        delay(latencyMillis)
        return GeneratedSong(
            words = listOf("사과"),
            onomatopoeia = listOf("둥둥"),
            seatZone = null,
            bpm = request.bpm,
            lyrics = listOf("사과는 맛있다", "사과는 빨갛다"),
            audioUrl = "",
        )
    }
}
