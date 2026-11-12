package com.hmgappchallenge.kidsrhythm.archive

import com.hmgappchallenge.kidsrhythm.server.SongJson
import com.hmgappchallenge.kidsrhythm.server.SongResult
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * 이 차에만 남는 보관함. 노래마다 mp3와 정보(JSON)를 앱 저장 공간 [dir]에 둔다.
 * 정보 JSON에는 서버 결과(가사 시간표 포함)를 그대로 넣어, 다시 듣기 화면을 서버 없이 그린다.
 */
class FileSongArchive(private val dir: File) : SongArchive {
    private val state = MutableStateFlow(loadAll())

    override val songs: StateFlow<List<ArchivedSong>> = state

    /**
     * 완성곡을 보관함에 넣는다. [audio]는 앱 캐시에 받아 둔 mp3.
     * mp3가 없거나 복사하지 못하면 [IOException]을 던지고, 보관함에는 아무것도 남기지 않는다.
     */
    suspend fun import(result: SongResult, audio: File, members: List<ArchivedMember>, createdAtMillis: Long = System.currentTimeMillis()): ArchivedSong =
        withContext(Dispatchers.IO) {
            dir.mkdirs()
            val id = createdAtMillis
            val target = File(dir, "$id.mp3")
            if (!audio.isFile) throw IOException("노래 파일이 없어요: ${audio.path}")
            try {
                audio.copyTo(target, overwrite = true)
            } catch (e: IOException) {
                target.delete()
                throw e
            }
            val song = ArchivedSong(
                id = id,
                title = result.title,
                createdAtMillis = createdAtMillis,
                durationMillis = result.durationMs,
                lyrics = result.lyrics.map { it.text },
                audioFilePath = target.path,
                lyricLines = result.lyrics,
                members = members,
                bpm = result.bpm,
                firstBeatMs = result.firstBeatMs,
            )
            writeInfo(song, result)
            state.update { list -> listOf(song) + list.filterNot { it.id == song.id } }
            song
        }

    override suspend fun save(song: ArchivedSong) = withContext(Dispatchers.IO) {
        dir.mkdirs()
        writeInfo(song, null)
        state.update { list -> (listOf(song) + list.filterNot { it.id == song.id }).sortedByDescending { it.createdAtMillis } }
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        File(dir, "$id.json").delete()
        File(dir, "$id.mp3").delete()
        state.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        dir.listFiles()?.forEach { it.delete() }
        state.value = emptyList()
    }

    override suspend fun totalBytes(): Long = withContext(Dispatchers.IO) {
        dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    private fun writeInfo(song: ArchivedSong, result: SongResult?) {
        val o = JSONObject()
            .put("id", song.id)
            .put("title", song.title)
            .put("createdAtMillis", song.createdAtMillis)
            .put("durationMillis", song.durationMillis)
            .put("audioFile", File(song.audioFilePath).name)
            .put("members", JSONArray(song.members.map { JSONObject().put("memberId", it.memberId).put("name", it.name).put("instrument", it.instrument) }))
        if (result != null) o.put("result", SongJson.toJson(result))
        else o.put("lyrics", JSONArray(song.lyrics))
        File(dir, "${song.id}.json").writeText(o.toString(), Charsets.UTF_8)
    }

    private fun loadAll(): List<ArchivedSong> {
        val files = dir.listFiles { f -> f.extension == "json" } ?: return emptyList()
        return files.mapNotNull { f ->
            try {
                val o = JSONObject(f.readText(Charsets.UTF_8))
                val audio = File(dir, o.optString("audioFile"))
                if (!audio.exists()) return@mapNotNull null
                val result = o.optJSONObject("result")?.let(SongJson::result)
                val membersArr = o.optJSONArray("members") ?: JSONArray()
                ArchivedSong(
                    id = o.optLong("id"),
                    title = o.optString("title"),
                    createdAtMillis = o.optLong("createdAtMillis"),
                    durationMillis = o.optLong("durationMillis"),
                    lyrics = result?.lyrics?.map { it.text } ?: o.optJSONArray("lyrics")?.let { a -> (0 until a.length()).map { a.optString(it) } }.orEmpty(),
                    audioFilePath = audio.path,
                    lyricLines = result?.lyrics.orEmpty(),
                    members = (0 until membersArr.length()).mapNotNull { i ->
                        membersArr.optJSONObject(i)?.let { ArchivedMember(it.optLong("memberId"), it.optString("name"), it.optString("instrument")) }
                    },
                    bpm = result?.bpm ?: 0,
                    firstBeatMs = result?.firstBeatMs ?: 0,
                )
            } catch (_: JSONException) {
                null
            }
        }.sortedByDescending { it.createdAtMillis }
    }
}
