package com.hmgappchallenge.kidsrhythm.server

import java.io.File
import kotlinx.coroutines.delay

/** 서버 없이 화면을 확인할 때 쓰는 가짜 구현. 정해진 단어와 결과를 돌려준다. */
class FakeSongService(private val latencyMillis: Long = 300L) : SongService {
    private val words = listOf(HeardWord("엄마", 1, "word"), HeardWord("둥둥", 1, "onomatopoeia"), HeardWord("랄랄라", 2, "onomatopoeia"), HeardWord("사과", 2, "word"))

    override suspend fun embedVoice(wav: ByteArray): VoiceEmbedResult {
        delay(latencyMillis)
        return VoiceEmbedResult(List(EMBEDDING_SIZE) { 0.01f * (it % 7) }, VoiceEmbedResult.QUALITY_OK, 6.0)
    }

    override suspend fun startSession(members: List<SessionMember>): SessionInfo = SessionInfo("fake", 2000, 180)

    override suspend fun sendChunk(sessionId: String, seq: Int, pcm: ByteArray): ChunkResult {
        delay(latencyMillis)
        val word = if (seq % 2 == 1) words.getOrNull(seq / 2) else null
        return ChunkResult(listOf(1L + seq % 2), false, listOfNotNull(word))
    }

    override suspend fun finish(sessionId: String, drive: DriveInfo, beat: BeatInfo): String = "fake-song"

    override suspend fun cancelSession(sessionId: String) = Unit

    override suspend fun status(songId: String): SongStatus {
        delay(latencyMillis)
        return SongStatus(songId, "DONE", "FINISH", 1.0, SAMPLE_RESULT, null)
    }

    override suspend fun downloadAudio(songId: String, result: SongResult, dest: File) = Unit

    override suspend fun deleteSong(songId: String) = Unit

    companion object {
        const val EMBEDDING_SIZE = 192

        val SAMPLE_RESULT = SongResult(
            title = "사과 노래",
            durationMs = 63_600,
            bpm = 110,
            beatType = 8,
            firstBeatMs = 0,
            lyrics = listOf(
                LyricLine("사과는 빨갛다, 둥둥 둥둥", 4_364, 8_727, "verse", listOf(Highlight(0, 2, 2, "word"), Highlight(9, 11, 1, "onomatopoeia"))),
                LyricLine("사과는 맛있다, 랄랄라 랄라", 8_727, 13_091, "verse", listOf(Highlight(0, 2, 2, "word"))),
                LyricLine("둥둥 둥둥, 사과 노래", 21_818, 26_182, "chorus", emptyList()),
                LyricLine("랄랄라 신나는 우리 노래", 26_182, 30_545, "chorus", emptyList()),
            ),
            words = listOf(HeardWord("사과", 2, "word")),
            onomatopoeia = listOf(HeardWord("둥둥", 1, "onomatopoeia"), HeardWord("랄랄라", 2, "onomatopoeia")),
            members = listOf(ResultMember(1, "DRUM", 4.2), ResultMember(2, "SHAKER", 3.1)),
            audioUrl = "",
        )
    }
}
