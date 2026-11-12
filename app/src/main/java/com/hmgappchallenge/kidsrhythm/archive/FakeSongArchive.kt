package com.hmgappchallenge.kidsrhythm.archive

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** 보관함 저장소가 정해지기 전까지 쓰는 메모리 구현. 홈 화면 확인용 샘플 한 곡을 담고 시작한다. */
class FakeSongArchive(nowMillis: Long = System.currentTimeMillis()) : SongArchive {
    private val state = MutableStateFlow(
        listOf(
            ArchivedSong(
                id = 1,
                title = "사과 노래",
                createdAtMillis = nowMillis - TimeUnit.DAYS.toMillis(1),
                durationMillis = TimeUnit.SECONDS.toMillis(102),
                lyrics = emptyList(),
                audioFilePath = "",
            ),
        ),
    )

    override val songs: StateFlow<List<ArchivedSong>> = state

    override suspend fun save(song: ArchivedSong) {
        state.update { list -> listOf(song) + list.filterNot { it.id == song.id } }
    }

    override suspend fun delete(id: Long) {
        state.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }

    override suspend fun totalBytes(): Long = 0L
}
