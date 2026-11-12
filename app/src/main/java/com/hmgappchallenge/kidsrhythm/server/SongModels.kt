package com.hmgappchallenge.kidsrhythm.server

import org.json.JSONArray
import org.json.JSONObject

/** 녹음 세션에 보내는 가족 한 명. 이름은 보내지 않는다 (docs/API.md 기본 원칙). */
data class SessionMember(val memberId: Long, val instrument: String, val embedding: List<Float>?)

data class SessionInfo(val sessionId: String, val chunkMs: Int, val maxRecordingSec: Int)

/** 녹음 중에 새로 들린 단어. [kind]는 word(명사) 또는 onomatopoeia(의성어). */
data class HeardWord(val text: String, val memberId: Long?, val kind: String)

data class ChunkResult(val speaking: List<Long>, val unregisteredVoice: Boolean, val newWords: List<HeardWord>)

data class VoiceEmbedResult(val embedding: List<Float>?, val quality: String, val speechSec: Double) {
    val isOk: Boolean get() = quality == QUALITY_OK && embedding != null

    companion object {
        const val QUALITY_OK = "ok"
    }
}

/** 녹음하는 동안 1초마다 잰 속도와 바깥 온도. */
data class DriveInfo(val speedKmh: List<Float>, val intervalSec: Float, val outsideTempC: Float?, val startedAtIso: String)

/** S03 화면에서 보여 준 비트. [beatType]은 2·4·8·16비트 (한 마디 박자 수가 아니다). */
data class BeatInfo(val bpm: Int, val beatType: Int)

/** 가사 한 줄 안에서 가족 목소리로 나온 단어 위치. [end]는 포함하지 않는다. */
data class Highlight(val start: Int, val end: Int, val memberId: Long?, val kind: String)

data class LyricLine(val text: String, val startMs: Long, val endMs: Long, val section: String, val highlights: List<Highlight>)

data class ResultMember(val memberId: Long, val instrument: String, val speechSec: Double)

data class SongResult(
    val title: String,
    val durationMs: Long,
    val bpm: Int,
    val beatType: Int,
    val firstBeatMs: Long,
    val lyrics: List<LyricLine>,
    val words: List<HeardWord>,
    val onomatopoeia: List<HeardWord>,
    val members: List<ResultMember>,
    val audioUrl: String,
)

data class SongError(val code: String, val message: String)

data class SongStatus(
    val songId: String,
    val status: String,
    val step: String?,
    val progress: Double,
    val result: SongResult?,
    val error: SongError?,
) {
    val isDone: Boolean get() = status == "DONE"
    val isFinished: Boolean get() = status == "DONE" || status == "FAILED" || status == "CANCELED"
}

/** 서버 JSON ↔ 모델. 보관함도 같은 모양으로 저장한다. */
object SongJson {
    private fun JSONObject.optLongOrNull(key: String): Long? = if (isNull(key) || !has(key)) null else optLong(key)

    private inline fun <T> JSONArray?.mapObjects(block: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { i -> optJSONObject(i)?.let(block) }
    }

    /** [defaultKind]: kind가 없을 때 쓴다 (결과의 onomatopoeia 목록은 kind 없이 온다). */
    fun heardWord(o: JSONObject, defaultKind: String = "word") =
        HeardWord(o.optString("text"), o.optLongOrNull("memberId"), o.optString("kind", defaultKind))

    fun chunkResult(o: JSONObject) = ChunkResult(
        speaking = o.optJSONArray("speaking")?.let { arr -> (0 until arr.length()).map { arr.optLong(it) } } ?: emptyList(),
        unregisteredVoice = o.optBoolean("unregisteredVoice"),
        newWords = o.optJSONArray("newWords").mapObjects { heardWord(it) },
    )

    fun voiceEmbed(o: JSONObject) = VoiceEmbedResult(
        embedding = o.optJSONArray("embedding")?.let { arr -> (0 until arr.length()).map { arr.optDouble(it).toFloat() } },
        quality = o.optString("quality"),
        speechSec = o.optDouble("speechSec", 0.0),
    )

    fun result(o: JSONObject) = SongResult(
        title = o.optString("title"),
        durationMs = o.optLong("durationMs"),
        bpm = o.optInt("bpm"),
        beatType = o.optInt("beatType", 4),
        firstBeatMs = o.optLong("firstBeatMs"),
        lyrics = o.optJSONArray("lyrics").mapObjects { l ->
            LyricLine(
                text = l.optString("text"),
                startMs = l.optLong("startMs"),
                endMs = l.optLong("endMs"),
                section = l.optString("section"),
                highlights = l.optJSONArray("highlights").mapObjects { h ->
                    Highlight(h.optInt("start"), h.optInt("end"), h.optLongOrNull("memberId"), h.optString("kind"))
                },
            )
        },
        words = o.optJSONArray("words").mapObjects { heardWord(it) },
        onomatopoeia = o.optJSONArray("onomatopoeia").mapObjects { heardWord(it, "onomatopoeia") },
        members = o.optJSONArray("members").mapObjects { m ->
            ResultMember(m.optLong("memberId"), m.optString("instrument"), m.optDouble("speechSec", 0.0))
        },
        audioUrl = o.optJSONObject("audio")?.optString("url").orEmpty(),
    )

    fun status(o: JSONObject) = SongStatus(
        songId = o.optString("songId"),
        status = o.optString("status"),
        step = if (o.isNull("step")) null else o.optString("step"),
        progress = o.optDouble("progress", 0.0),
        result = o.optJSONObject("result")?.let(::result),
        error = o.optJSONObject("error")?.let { SongError(it.optString("code"), it.optString("message")) },
    )

    private fun wordJson(w: HeardWord) = JSONObject().put("text", w.text).put("memberId", w.memberId ?: JSONObject.NULL).put("kind", w.kind)

    fun toJson(r: SongResult): JSONObject = JSONObject()
        .put("title", r.title)
        .put("durationMs", r.durationMs)
        .put("bpm", r.bpm)
        .put("beatType", r.beatType)
        .put("firstBeatMs", r.firstBeatMs)
        .put(
            "lyrics",
            JSONArray(
                r.lyrics.map { l ->
                    JSONObject()
                        .put("text", l.text)
                        .put("startMs", l.startMs)
                        .put("endMs", l.endMs)
                        .put("section", l.section)
                        .put(
                            "highlights",
                            JSONArray(
                                l.highlights.map { h ->
                                    JSONObject().put("start", h.start).put("end", h.end)
                                        .put("memberId", h.memberId ?: JSONObject.NULL).put("kind", h.kind)
                                },
                            ),
                        )
                },
            ),
        )
        .put("words", JSONArray(r.words.map(::wordJson)))
        .put("onomatopoeia", JSONArray(r.onomatopoeia.map(::wordJson)))
        .put("members", JSONArray(r.members.map { JSONObject().put("memberId", it.memberId).put("instrument", it.instrument).put("speechSec", it.speechSec) }))
        .put("audio", JSONObject().put("url", r.audioUrl))

    fun sessionMembers(members: List<SessionMember>): JSONArray = JSONArray(
        members.map { m ->
            JSONObject()
                .put("memberId", m.memberId)
                .put("instrument", m.instrument)
                .put("embedding", m.embedding?.let { e -> JSONArray(e.map { it.toDouble() }) } ?: JSONObject.NULL)
        },
    )

    fun drive(d: DriveInfo): JSONObject = JSONObject()
        .put("speedKmh", JSONArray(d.speedKmh.map { it.toDouble() }))
        .put("intervalSec", d.intervalSec.toDouble())
        .put("outsideTempC", d.outsideTempC?.toDouble() ?: JSONObject.NULL)
        .put("startedAt", d.startedAtIso)

    fun beat(b: BeatInfo): JSONObject = JSONObject().put("bpm", b.bpm).put("beatType", b.beatType)
}
