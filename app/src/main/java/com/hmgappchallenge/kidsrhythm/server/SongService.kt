package com.hmgappchallenge.kidsrhythm.server

import java.io.File

/** 노래 생성 서버와 주고받는 일 (docs/API.md v0.2). 실제 구현은 [SongApi], 시험용은 [FakeSongService]. */
interface SongService {
    /** 서버에 닿는지 본다 (`GET /v1/health`, 키 없이). 닿지 않으면 false. */
    suspend fun isReachable(): Boolean

    /** 가족 목소리 등록 녹음(wav)으로 목소리 특징을 만든다. */
    suspend fun embedVoice(wav: ByteArray): VoiceEmbedResult

    /** S04 녹음을 시작할 때 세션을 연다. */
    suspend fun startSession(members: List<SessionMember>): SessionInfo

    /** 2초 녹음 조각(PCM 16비트, 16kHz, 모노)을 보낸다. 누가 말했는지와 새 단어가 돌아온다. */
    suspend fun sendChunk(sessionId: String, seq: Int, pcm: ByteArray): ChunkResult

    /** 완성하기. 노래 작업 번호를 돌려준다. */
    suspend fun finish(sessionId: String, drive: DriveInfo, beat: BeatInfo): String

    /** S04에서 그만둘 때. 서버가 모아 둔 녹음을 바로 지운다. */
    suspend fun cancelSession(sessionId: String)

    suspend fun status(songId: String): SongStatus

    /** 완성곡 mp3를 [dest]에 받는다. */
    suspend fun downloadAudio(songId: String, result: SongResult, dest: File)

    /** 만드는 중이면 멈추고, 완성됐으면 서버에 남은 mp3를 지운다. */
    suspend fun deleteSong(songId: String)
}

/** 서버 오류. [code]는 docs/API.md의 오류 코드 (NOT_FOUND, TOO_LONG …), 연결 실패는 NETWORK. */
class ServerException(val httpStatus: Int, val code: String, message: String, cause: Throwable? = null) : Exception(message, cause) {
    val isNetwork: Boolean get() = code == NETWORK

    companion object {
        const val NETWORK = "NETWORK"
    }
}
