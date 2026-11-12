package com.hmgappchallenge.kidsrhythm.server

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 서버 응답 모양(docs/API.md)을 앱 모델로 바르게 읽는지 확인한다. */
class SongJsonTest {
    // docs/API.md 5번 "완성 (DONE)" 예시 그대로
    private val doneJson = """
        {
          "songId": "c7f3a9", "status": "DONE", "step": "FINISH", "progress": 1.0,
          "result": {
            "title": "사과 노래", "durationMs": 102000, "bpm": 110, "beatType": 8, "firstBeatMs": 0,
            "lyrics": [
              { "text": "사과는 빨갛다, 둥둥 둥둥", "startMs": 4364, "endMs": 8727, "section": "verse",
                "highlights": [
                  { "start": 0, "end": 2, "memberId": 2, "kind": "word" },
                  { "start": 9, "end": 11, "memberId": 1, "kind": "onomatopoeia" },
                  { "start": 12, "end": 14, "memberId": 1, "kind": "onomatopoeia" } ] },
              { "text": "사과는 맛있다, 랄랄라 랄라", "startMs": 8727, "endMs": 13091, "section": "verse",
                "highlights": [
                  { "start": 0, "end": 2, "memberId": 2, "kind": "word" },
                  { "start": 9, "end": 12, "memberId": 3, "kind": "onomatopoeia" } ] }
            ],
            "words": [{ "text": "사과", "memberId": 2 }],
            "onomatopoeia": [{ "text": "둥둥", "memberId": 1 }, { "text": "랄랄라", "memberId": 3 }],
            "members": [
              { "memberId": 1, "instrument": "DRUM", "speechSec": 4.2 },
              { "memberId": 2, "instrument": "SHAKER", "speechSec": 3.1 },
              { "memberId": 3, "instrument": "TAMBOURINE", "speechSec": 2.0 } ],
            "audio": { "url": "/v1/songs/c7f3a9/audio", "format": "mp3", "expiresInSec": 600 }
          },
          "error": null
        }
    """.trimIndent()

    @Test
    fun `완성된 노래 응답을 읽는다`() {
        val status = SongJson.status(JSONObject(doneJson))
        assertTrue(status.isDone)
        assertTrue(status.isFinished)
        assertEquals("FINISH", status.step)
        assertNull(status.error)
        val result = requireNotNull(status.result)
        assertEquals("사과 노래", result.title)
        assertEquals(102_000L, result.durationMs)
        assertEquals(110, result.bpm)
        assertEquals(8, result.beatType)
        assertEquals("/v1/songs/c7f3a9/audio", result.audioUrl)
        assertEquals(listOf(1L, 2L, 3L), result.members.map { it.memberId })
    }

    @Test
    fun `가사 강조 위치로 가족이 말한 단어를 잘라 낼 수 있다`() {
        val line = requireNotNull(SongJson.status(JSONObject(doneJson)).result).lyrics.first()
        assertEquals(4_364L, line.startMs)
        assertEquals("verse", line.section)
        assertEquals(listOf("사과", "둥둥", "둥둥"), line.highlights.map { line.text.substring(it.start, it.end) })
        assertEquals(2L, line.highlights.first().memberId)
    }

    @Test
    fun `kind가 없는 결과 단어 목록은 목록 이름으로 종류를 정한다`() {
        val result = requireNotNull(SongJson.status(JSONObject(doneJson)).result)
        assertEquals(listOf("word"), result.words.map { it.kind })
        assertEquals(listOf("onomatopoeia", "onomatopoeia"), result.onomatopoeia.map { it.kind })
    }

    @Test
    fun `보관함에 저장한 모양을 다시 읽으면 같은 결과가 나온다`() {
        val result = requireNotNull(SongJson.status(JSONObject(doneJson)).result)
        assertEquals(result, SongJson.result(JSONObject(SongJson.toJson(result).toString())))
    }

    @Test
    fun `만드는 중과 실패 응답을 읽는다`() {
        val running = SongJson.status(
            JSONObject("""{ "songId": "c7f3a9", "status": "RUNNING", "step": "SPLIT_VOICES", "progress": 0.55, "result": null, "error": null }"""),
        )
        assertFalse(running.isFinished)
        assertEquals("SPLIT_VOICES", running.step)
        assertNull(running.result)

        val failed = SongJson.status(
            JSONObject(
                """{ "songId": "c7f3a9", "status": "FAILED", "step": "FIND_WORDS", "progress": 0.1, "result": null,
                    "error": { "code": "NO_SPEECH", "message": "목소리가 들리지 않았어요" } }""",
            ),
        )
        assertTrue(failed.isFinished)
        assertFalse(failed.isDone)
        assertEquals("NO_SPEECH", failed.error?.code)
    }

    @Test
    fun `녹음 조각 응답에서 지금 말하는 가족과 새 단어를 읽는다`() {
        val chunk = SongJson.chunkResult(
            JSONObject(
                """{ "speaking": [1, 3], "unregisteredVoice": true,
                    "newWords": [{ "text": "사과", "memberId": 2, "kind": "word" }, { "text": "둥둥", "memberId": null, "kind": "onomatopoeia" }] }""",
            ),
        )
        assertEquals(listOf(1L, 3L), chunk.speaking)
        assertTrue(chunk.unregisteredVoice)
        assertEquals(HeardWord("사과", 2L, "word"), chunk.newWords[0])
        assertEquals(HeardWord("둥둥", null, "onomatopoeia"), chunk.newWords[1])
    }

    @Test
    fun `목소리 등록 결과는 ok이고 특징이 있을 때만 쓴다`() {
        val ok = SongJson.voiceEmbed(JSONObject("""{ "embedding": [0.1, -0.2, 0.3], "quality": "ok", "speechSec": 4.1 }"""))
        assertTrue(ok.isOk)
        assertEquals(3, ok.embedding?.size)
        val short = SongJson.voiceEmbed(JSONObject("""{ "embedding": null, "quality": "too_short", "speechSec": 1.2 }"""))
        assertFalse(short.isOk)
        assertNull(short.embedding)
    }

    @Test
    fun `세션에 보내는 가족에는 이름이 없고 목소리가 없으면 null을 보낸다`() {
        val json = SongJson.sessionMembers(
            listOf(SessionMember(1, "DRUM", listOf(0.5f, 0.25f)), SessionMember(2, "SHAKER", null)),
        )
        val first = json.getJSONObject(0)
        assertEquals(setOf("memberId", "instrument", "embedding"), first.keys().asSequence().toSet())
        assertEquals(0.25, first.getJSONArray("embedding").getDouble(1), 1e-6)
        assertTrue(json.getJSONObject(1).isNull("embedding"))
    }

    @Test
    fun `주행 정보는 바깥 온도를 모르면 null로 보낸다`() {
        val json = SongJson.drive(DriveInfo(listOf(0f, 12.5f), intervalSec = 1f, outsideTempC = null, startedAtIso = "2026-11-11T09:00:00+09:00"))
        assertEquals(12.5, json.getJSONArray("speedKmh").getDouble(1), 1e-6)
        assertTrue(json.isNull("outsideTempC"))
        assertEquals("2026-11-11T09:00:00+09:00", json.getString("startedAt"))
        assertEquals(8, SongJson.beat(BeatInfo(bpm = 110, beatType = 8)).getInt("beatType"))
    }
}
