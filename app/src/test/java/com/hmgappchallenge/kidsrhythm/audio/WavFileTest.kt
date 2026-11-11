package com.hmgappchallenge.kidsrhythm.audio

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WavFileTest {
    private fun tempFile(bytes: ByteArray): File =
        File.createTempFile("wav-test", ".wav").apply {
            deleteOnExit()
            writeBytes(bytes)
        }

    private fun le(bytes: ByteArray) = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

    @Test
    fun `wav 머리말에 16kHz 모노 16비트와 크기를 적는다`() {
        val pcm = ByteArray(3_200) { (it % 7).toByte() }
        val wav = pcm16ToWav(pcm)
        assertEquals(44 + pcm.size, wav.size)
        assertEquals("RIFF", String(wav, 0, 4))
        assertEquals("WAVE", String(wav, 8, 4))
        assertEquals(36 + pcm.size, le(wav).getInt(4))
        assertEquals(1, le(wav).getShort(22).toInt())
        assertEquals(16_000, le(wav).getInt(24))
        assertEquals(16, le(wav).getShort(34).toInt())
        assertEquals(pcm.size, le(wav).getInt(40))
    }

    @Test
    fun `만든 wav를 다시 읽으면 같은 PCM이 나온다`() {
        val pcm = ByteArray(6_400) { (it * 31).toByte() }
        assertArrayEquals(pcm, readPcm16Wav(tempFile(pcm16ToWav(pcm))))
    }

    @Test
    fun `fmt와 data 사이에 다른 조각이 있어도 읽는다`() {
        // ffmpeg 등이 만드는 wav에는 LIST 조각이 끼어 있다
        val pcm = ByteArray(1_600) { (it * 3).toByte() }
        val wav = pcm16ToWav(pcm)
        val list = le(ByteArray(12)).put("LIST".toByteArray()).putInt(4).put("INFO".toByteArray()).array()
        val withList = wav.copyOfRange(0, 36) + list + wav.copyOfRange(36, wav.size)
        assertArrayEquals(pcm, readPcm16Wav(tempFile(withList)))
    }

    @Test
    fun `data 크기가 -1이면 파일 끝까지 읽는다`() {
        val pcm = ByteArray(800) { 5 }
        val wav = pcm16ToWav(pcm)
        le(wav).putInt(40, -1)
        assertArrayEquals(pcm, readPcm16Wav(tempFile(wav)))
    }

    @Test
    fun `샘플레이트가 다르면 읽지 않는다`() {
        assertNull(readPcm16Wav(tempFile(pcm16ToWav(ByteArray(1_000), sampleRate = 44_100))))
    }

    @Test
    fun `wav가 아니거나 조각 크기가 깨져도 멈추지 않고 null을 준다`() {
        assertNull(readPcm16Wav(tempFile("this is not a wave file, just some text.......".toByteArray())))
        val broken = pcm16ToWav(ByteArray(100))
        le(broken).putInt(16, -8) // fmt 조각 크기
        assertNull(readPcm16Wav(tempFile(broken)))
    }

    @Test
    fun `조용하면 음량은 -90 dB이다`() {
        assertEquals(-90f, PcmRecorder.levelDb(ByteArray(3_200), 3_200), 0.001f)
        assertEquals(-90f, PcmRecorder.levelDb(ByteArray(0), 0), 0.001f)
    }

    private fun square(amplitude: Int, bytes: Int = 3_200): ByteArray {
        val b = le(ByteArray(bytes))
        for (i in 0 until bytes / 2) b.putShort((if (i % 2 == 0) amplitude else -amplitude).toShort())
        return b.array()
    }

    @Test
    fun `가장 큰 소리는 0 dB, 절반 크기는 -6 dB 가까이 나온다`() {
        assertEquals(0f, PcmRecorder.levelDb(square(32_767), 3_200), 0.01f)
        assertEquals(-6.02f, PcmRecorder.levelDb(square(16_384), 3_200), 0.05f)
    }

    @Test
    fun `음량은 length 앞부분만 잰다`() {
        val loudThenSilent = square(32_767, 1_600) + ByteArray(1_600)
        assertEquals(0f, PcmRecorder.levelDb(loudThenSilent, 1_600), 0.01f)
    }
}
