package com.hmgappchallenge.kidsrhythm.archive

import com.hmgappchallenge.kidsrhythm.server.LyricLine
import kotlinx.coroutines.flow.Flow

/** 노래에 함께한 가족 (저장할 때의 이름과 악기). */
data class ArchivedMember(val memberId: Long, val name: String, val instrument: String)

/** 기획서 07번 보관함 항목: 제목 · 날짜 · 길이 · 가사 + 로컬 오디오 파일. */
data class ArchivedSong(
    val id: Long,
    val title: String,
    val createdAtMillis: Long,
    val durationMillis: Long,
    val lyrics: List<String>,
    val audioFilePath: String,
    /** 줄마다 시간과 강조 위치가 붙은 가사 (다시 듣기에서 지금 부르는 줄 강조, 줄 눌러 듣기). */
    val lyricLines: List<LyricLine> = emptyList(),
    val members: List<ArchivedMember> = emptyList(),
    val bpm: Int = 0,
    val firstBeatMs: Long = 0,
)

interface SongArchive {
    val songs: Flow<List<ArchivedSong>>
    suspend fun save(song: ArchivedSong)
    suspend fun delete(id: Long)
}
