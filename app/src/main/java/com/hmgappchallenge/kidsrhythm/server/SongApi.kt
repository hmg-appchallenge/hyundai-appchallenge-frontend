package com.hmgappchallenge.kidsrhythm.server

import com.hmgappchallenge.kidsrhythm.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/**
 * 서버 주소. 기본값 http://10.0.2.2:8000은 에뮬레이터 안에서 본 같은 PC다.
 * 다른 기기에서 쓰려면 local.properties에 kidsrhythm.serverUrl을 적는다 (app/build.gradle.kts 참고).
 */
object ServerConfig {
    val BASE_URL: String = BuildConfig.SERVER_URL.trimEnd('/')

    /** 서버에 KR_API_KEY를 정했으면 local.properties의 kidsrhythm.apiKey에 같은 값을 넣는다. 비어 있으면 헤더를 보내지 않는다. */
    val API_KEY: String = BuildConfig.SERVER_API_KEY

    const val CONNECT_TIMEOUT_MS = 5_000
    const val READ_TIMEOUT_MS = 20_000

    /**
     * 연결이 잠깐 끊겨도 바로 실패하지 않게 같은 요청을 다시 보낸다 (최대 횟수).
     * 서버는 같은 조각(seq)·완성하기를 다시 받아도 한 번만 처리한다 (docs/API.md 3-2, 3-3).
     */
    const val NETWORK_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 300L
}

/** [SongService]의 HTTP 구현. 추가 라이브러리 없이 HttpURLConnection과 org.json만 쓴다. */
class SongApi(
    private val baseUrl: String = ServerConfig.BASE_URL,
    private val apiKey: String = ServerConfig.API_KEY,
) : SongService {

    private class Reply(val code: Int, val body: ByteArray)

    /** 연결 오류면 [ServerConfig.NETWORK_ATTEMPTS]번까지 다시 보낸다. 서버가 준 오류(4xx·5xx)는 다시 보내지 않는다. */
    private suspend fun call(method: String, path: String, body: ByteArray? = null, contentType: String? = null, readTimeoutMs: Int = ServerConfig.READ_TIMEOUT_MS): Reply {
        var attempt = 0
        while (true) {
            try {
                return callOnce(method, path, body, contentType, readTimeoutMs)
            } catch (e: ServerException) {
                attempt++
                if (!e.isNetwork || attempt >= ServerConfig.NETWORK_ATTEMPTS) throw e
                delay(ServerConfig.RETRY_DELAY_MS * attempt)
            }
        }
    }

    private fun callOnce(method: String, path: String, body: ByteArray?, contentType: String?, readTimeoutMs: Int): Reply {
        val conn = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection)
        try {
            conn.requestMethod = method
            conn.connectTimeout = ServerConfig.CONNECT_TIMEOUT_MS
            conn.readTimeout = readTimeoutMs
            if (apiKey.isNotEmpty()) conn.setRequestProperty("X-Api-Key", apiKey)
            conn.setRequestProperty("Accept", "application/json")
            if (body != null) {
                conn.doOutput = true
                if (contentType != null) conn.setRequestProperty("Content-Type", contentType)
                conn.setFixedLengthStreamingMode(body.size)
                conn.outputStream.use { it.write(body) }
            }
            val code = conn.responseCode
            val stream = if (code >= 400) conn.errorStream else conn.inputStream
            val bytes = stream?.use { it.readBytes() } ?: ByteArray(0)
            return Reply(code, bytes)
        } catch (e: IOException) {
            throw ServerException(0, ServerException.NETWORK, "서버에 연결할 수 없어요", e)
        } finally {
            conn.disconnect()
        }
    }

    private fun Reply.json(): JSONObject {
        val text = body.toString(Charsets.UTF_8)
        if (code !in 200..299) {
            val err = try { JSONObject(text) } catch (_: JSONException) { null }
            throw ServerException(code, err?.optString("code").orEmpty().ifEmpty { "HTTP_$code" }, err?.optString("message").orEmpty().ifEmpty { "서버 오류 ($code)" })
        }
        return try { JSONObject(text) } catch (e: JSONException) { throw ServerException(code, "BAD_RESPONSE", "서버 응답을 읽을 수 없어요", e) }
    }

    private fun Reply.ensureOk() {
        if (code !in 200..299) json()
    }

    private fun jsonBody(o: JSONObject) = o.toString().toByteArray(Charsets.UTF_8)

    override suspend fun embedVoice(wav: ByteArray): VoiceEmbedResult = withContext(Dispatchers.IO) {
        val boundary = UUID.randomUUID().toString()
        val out = ByteArrayOutputStream()
        out.write("--$boundary\r\nContent-Disposition: form-data; name=\"audio\"; filename=\"voice.wav\"\r\nContent-Type: audio/wav\r\n\r\n".toByteArray())
        out.write(wav)
        out.write("\r\n--$boundary--\r\n".toByteArray())
        SongJson.voiceEmbed(call("POST", "/v1/voices/embed", out.toByteArray(), "multipart/form-data; boundary=$boundary").json())
    }

    override suspend fun startSession(members: List<SessionMember>): SessionInfo = withContext(Dispatchers.IO) {
        val o = call("POST", "/v1/sessions", jsonBody(JSONObject().put("members", SongJson.sessionMembers(members))), "application/json").json()
        SessionInfo(o.optString("sessionId"), o.optInt("chunkMs", 2000), o.optInt("maxRecordingSec", 180))
    }

    override suspend fun sendChunk(sessionId: String, seq: Int, pcm: ByteArray): ChunkResult = withContext(Dispatchers.IO) {
        SongJson.chunkResult(call("POST", "/v1/sessions/$sessionId/chunks?seq=$seq", pcm, "application/octet-stream").json())
    }

    override suspend fun finish(sessionId: String, drive: DriveInfo, beat: BeatInfo): String = withContext(Dispatchers.IO) {
        val body = JSONObject().put("drive", SongJson.drive(drive)).put("beat", SongJson.beat(beat))
        call("POST", "/v1/sessions/$sessionId/finish", jsonBody(body), "application/json", readTimeoutMs = 60_000).json().optString("songId")
    }

    override suspend fun cancelSession(sessionId: String) = withContext(Dispatchers.IO) {
        call("DELETE", "/v1/sessions/$sessionId").ensureOk()
    }

    override suspend fun status(songId: String): SongStatus = withContext(Dispatchers.IO) {
        SongJson.status(call("GET", "/v1/songs/$songId").json())
    }

    override suspend fun downloadAudio(songId: String, result: SongResult, dest: File) = withContext(Dispatchers.IO) {
        val path = result.audioUrl.ifEmpty { "/v1/songs/$songId/audio" }
        val reply = call("GET", path, readTimeoutMs = 60_000)
        reply.ensureOk()
        dest.parentFile?.mkdirs()
        val tmp = File(dest.path + ".part")
        tmp.writeBytes(reply.body)
        if (!tmp.renameTo(dest)) {
            tmp.copyTo(dest, overwrite = true)
            tmp.delete()
        }
    }

    override suspend fun deleteSong(songId: String) = withContext(Dispatchers.IO) {
        call("DELETE", "/v1/songs/$songId").ensureOk()
    }
}
