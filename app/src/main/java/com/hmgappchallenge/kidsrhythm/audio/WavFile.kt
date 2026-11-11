package com.hmgappchallenge.kidsrhythm.audio

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** PCM 16비트 모노를 wav로 감싼다 (목소리 등록 업로드용). */
fun pcm16ToWav(pcm: ByteArray, sampleRate: Int = PcmRecorder.SAMPLE_RATE): ByteArray {
    val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
    header.put("RIFF".toByteArray()).putInt(36 + pcm.size).put("WAVE".toByteArray())
    header.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1).putInt(sampleRate).putInt(sampleRate * 2).putShort(2).putShort(16)
    header.put("data".toByteArray()).putInt(pcm.size)
    return header.array() + pcm
}

/** 16kHz 모노 16비트 wav의 PCM 부분만 읽는다 (디버그용 가짜 마이크). 형식이 다르면 null. */
fun readPcm16Wav(file: File): ByteArray? {
    val bytes = file.readBytes()
    if (bytes.size < 44 || String(bytes, 0, 4) != "RIFF" || String(bytes, 8, 4) != "WAVE") return null
    var pos = 12
    var data: ByteArray? = null
    var format: ByteBuffer? = null
    while (pos + 8 <= bytes.size) {
        val id = String(bytes, pos, 4)
        val size = ByteBuffer.wrap(bytes, pos + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
        val start = pos + 8
        // 크기가 깨졌거나(음수) 파일보다 크면 파일 끝까지로 본다. 파이프로 만든 wav는 data 크기가 -1로 오기도 한다
        val end = if (size < 0 || size > bytes.size - start) bytes.size else start + size
        // slice()로 잘라야 아래 getShort(2) 같은 위치가 조각 안에서 센 위치가 된다
        if (id == "fmt ") format = ByteBuffer.wrap(bytes, start, end - start).slice().order(ByteOrder.LITTLE_ENDIAN)
        if (id == "data") data = bytes.copyOfRange(start, end)
        if (end >= bytes.size) break
        pos = end + (size and 1)
    }
    val fmt = format?.takeIf { it.limit() >= 16 } ?: return null
    val channels = fmt.getShort(2).toInt()
    val rate = fmt.getInt(4)
    val bits = fmt.getShort(14).toInt()
    return if (channels == 1 && rate == PcmRecorder.SAMPLE_RATE && bits == 16) data else null
}
