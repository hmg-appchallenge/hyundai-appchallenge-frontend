package com.hmgappchallenge.kidsrhythm.archive

import com.hmgappchallenge.kidsrhythm.server.SongJson
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 앱을 처음 켰을 때 보관함이 비어 보이지 않게 예시 곡을 한 번 넣는다.
 *
 * 예시 곡은 `res/raw/sample_song.mp3`와 `res/raw/sample_song_info.json`이다 (11/12 고정 서버로 만든 '우리 바다 가자').
 * 넣고 나면 [marker] 파일을 남겨서, 사용자가 지워도 다시 넣지 않는다. 앱 데이터를 지우면 다시 들어간다.
 */
class SampleSongSeeder(
    private val archive: FileSongArchive,
    private val marker: File,
    private val readInfo: () -> String,
    private val openAudio: () -> InputStream,
    private val workDir: File,
) {
    /** 아직 안 넣었으면 예시 곡을 보관함에 넣는다. 넣었으면 true. */
    suspend fun seedOnce(): Boolean = withContext(Dispatchers.IO) {
        if (marker.exists()) return@withContext false
        val info = JSONObject(readInfo())
        val result = SongJson.result(info.getJSONObject("result"))
        val membersJson = info.getJSONArray("members")
        val members = (0 until membersJson.length()).map { i ->
            val m = membersJson.getJSONObject(i)
            ArchivedMember(m.getLong("memberId"), m.optString("name"), m.getString("instrument"))
        }
        workDir.mkdirs()
        val audio = File(workDir, "sample_song.mp3")
        try {
            openAudio().use { input -> audio.outputStream().use { input.copyTo(it) } }
            archive.import(result, audio, members, info.getLong("createdAtMillis"))
        } finally {
            audio.delete()
        }
        marker.parentFile?.mkdirs()
        marker.createNewFile()
        true
    }
}
