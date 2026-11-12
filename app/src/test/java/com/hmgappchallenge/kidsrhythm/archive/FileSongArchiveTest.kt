package com.hmgappchallenge.kidsrhythm.archive

import com.hmgappchallenge.kidsrhythm.server.LyricLine
import com.hmgappchallenge.kidsrhythm.server.SongResult
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FileSongArchiveTest {
    private val root = Files.createTempDirectory("archive").toFile()
    private val dir = File(root, "songs")

    private val result = SongResult(
        title = "사과 노래", durationMs = 60_000, bpm = 110, beatType = 8, firstBeatMs = 0,
        lyrics = listOf(LyricLine("사과는 빨갛다", 0, 10_000, "verse", emptyList())),
        words = emptyList(), onomatopoeia = emptyList(), members = emptyList(), audioUrl = "",
    )

    @Test
    fun `받아 둔 노래 파일이 없으면 저장하지 않고 오류를 알린다`() = runBlocking {
        val archive = FileSongArchive(dir)
        try {
            archive.import(result, File(root, "missing.mp3"), emptyList(), createdAtMillis = 1L)
            fail("노래 파일이 없는데 저장됐어요")
        } catch (_: IOException) {
        }
        assertTrue(archive.songs.value.isEmpty())
        assertTrue(dir.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun `노래 파일이 있으면 보관함에 넣는다`() = runBlocking {
        val audio = File(root, "song.mp3").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val archive = FileSongArchive(dir)
        archive.import(result, audio, emptyList(), createdAtMillis = 2L)
        assertEquals(listOf("사과 노래"), archive.songs.value.map { it.title })
        assertTrue(File(dir, "2.mp3").isFile)
    }
}
