package com.hmgappchallenge.kidsrhythm.archive

import com.hmgappchallenge.kidsrhythm.server.SongJson
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleSongSeederTest {
    /** 앱에 넣은 진짜 예시 곡 정보 (단위 테스트는 app 폴더에서 돈다). */
    private val infoText = File("src/main/res/raw/sample_song_info.json").readText()
    private val audioBytes = ByteArray(1024) { it.toByte() }
    private val root = Files.createTempDirectory("seed").toFile()

    private fun seeder(archive: FileSongArchive) = SampleSongSeeder(
        archive = archive,
        marker = File(root, "sample_song_seeded"),
        readInfo = { infoText },
        openAudio = { audioBytes.inputStream() },
        workDir = File(root, "cache"),
    )

    // 코루틴 블록은 영어 이름 함수 안에 둔다. 한글 테스트 이름 안에 두면 클래스 파일 이름에 한글이 들어가서
    // 파일 이름 인코딩이 UTF-8이 아닌 환경에서 컴파일이 깨진다.
    private fun seedNow(archive: FileSongArchive): Boolean = runBlocking { seeder(archive).seedOnce() }

    private fun deleteNow(archive: FileSongArchive, id: Long) = runBlocking { archive.delete(id) }

    @Test
    fun `처음 켜면 예시 곡이 가사·가족과 함께 보관함에 들어간다`() {
        val archive = FileSongArchive(File(root, "songs"))

        assertTrue(seedNow(archive))

        val song = archive.songs.value.single()
        assertEquals("우리 바다 가자", song.title)
        assertEquals(1_794_458_985_000L, song.createdAtMillis)
        assertEquals(13, song.lyricLines.size)
        assertEquals(listOf("아이", "엄마", "아빠"), song.members.map { it.name })
        assertTrue(File(song.audioFilePath).readBytes().contentEquals(audioBytes))
        // 앱을 다시 켜도 파일에서 그대로 읽힌다
        assertEquals("우리 바다 가자", FileSongArchive(File(root, "songs")).songs.value.single().title)
        // 잠깐 풀어 둔 mp3는 남기지 않는다
        assertFalse(File(root, "cache/sample_song.mp3").exists())
    }

    @Test
    fun `한 번 넣은 뒤에는 지워도 다시 넣지 않는다`() {
        val archive = FileSongArchive(File(root, "songs"))
        seedNow(archive)
        deleteNow(archive, archive.songs.value.single().id)

        assertFalse(seedNow(archive))
        assertTrue(archive.songs.value.isEmpty())
    }

    @Test
    fun `예시 곡 정보는 서버 응답 모양이고 강조 위치가 가사 글자와 맞는다`() {
        val result = SongJson.result(JSONObject(infoText).getJSONObject("result"))
        val heard = (result.words + result.onomatopoeia).map { it.text }.toSet()

        assertEquals(110, result.bpm)
        assertEquals(8, result.beatType)
        assertEquals(300L, result.firstBeatMs)
        assertEquals(61_100L, result.durationMs)
        result.lyrics.zipWithNext().forEach { (a, b) -> assertEquals(a.endMs, b.startMs) }
        result.lyrics.flatMap { line -> line.highlights.map { line.text.substring(it.start, it.end) } }
            .forEach { assertTrue("$it 은 녹음에서 들린 말이어야 해요", it in heard) }
    }
}
