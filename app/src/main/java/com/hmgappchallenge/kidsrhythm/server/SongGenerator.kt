package com.hmgappchallenge.kidsrhythm.server

import java.io.File
import kotlinx.coroutines.delay

/** 요청 형식은 임시다. AI 서버 담당자와 API를 맞추면 수정한다. */
data class SongRequest(
    val recording: File,
    val bpm: Int,
)

/**
 * 노래 생성 결과. API 명세 초안(GET /v1/songs/{songId}의 done 응답) 중 화면에 쓰는 값만 둔다.
 * [title]은 서버가 지어 준 제목이며, 들린 단어가 없으면 null이다.
 */
data class GeneratedSong(
    val title: String?,
    val words: List<String>,
    val onomatopoeia: List<String>,
    val bpm: Int,
    val durationSec: Int,
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
            title = null,
            words = listOf("사과"),
            onomatopoeia = listOf("둥둥"),
            bpm = request.bpm,
            durationSec = 102,
            lyrics = listOf(
                "사과는 빨갛다, 둥둥 둥둥",
                "사과는 맛있다, 랄랄라 랄라",
                "사과는 빨갛다, 둥둥 둥둥",
                "사과는 맛있다, 랄랄라 랄라",
            ),
            audioUrl = "",
        )
    }
}
