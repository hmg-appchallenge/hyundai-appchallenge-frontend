package com.hmgappchallenge.kidsrhythm.archive

import kotlinx.coroutines.flow.Flow

/** 기획서 07번 보관함 항목: 제목 · 날짜 · 길이 · 가사 + 로컬 오디오 파일. */
data class ArchivedSong(
    val id: Long,
    val title: String,
    val createdAtMillis: Long,
    val durationMillis: Long,
    val lyrics: List<String>,
    val audioFilePath: String,
)

interface SongArchive {
    val songs: Flow<List<ArchivedSong>>
    suspend fun save(song: ArchivedSong)
    suspend fun delete(id: Long)
}
