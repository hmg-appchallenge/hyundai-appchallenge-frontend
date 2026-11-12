package com.hmgappchallenge.kidsrhythm.family

import android.content.Context
import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** 노래에 함께하는 가족 목록. 목소리 설정(S02-1~3)에서 바꾸고, 비트·녹음 화면에서 읽는다. */
interface FamilyRepository {
    val members: StateFlow<List<FamilyMember>>

    /** 자리가 없으면(최대 [MAX_FAMILY_MEMBERS]명) 추가하지 않고 false. */
    fun add(name: String, instrument: Instrument, hasVoice: Boolean, embedding: List<Float>? = null): Boolean
    fun update(member: FamilyMember)
    fun delete(id: Long)
    /** 가족을 모두 지운다 (이름·목소리 특징·악기). */
    fun deleteAll()
}

const val MAX_FAMILY_MEMBERS = 4

/** 앱을 끄면 사라지는 메모리 저장소 (테스트·미리 보기용). */
open class InMemoryFamilyRepository(initial: List<FamilyMember> = emptyList()) : FamilyRepository {
    protected val state = MutableStateFlow(initial.take(MAX_FAMILY_MEMBERS))
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    override val members: StateFlow<List<FamilyMember>> = state

    override fun add(name: String, instrument: Instrument, hasVoice: Boolean, embedding: List<Float>?): Boolean {
        if (state.value.size >= MAX_FAMILY_MEMBERS) return false
        state.update { it + FamilyMember(nextId++, name, instrument, hasVoice, embedding) }
        changed()
        return true
    }

    override fun update(member: FamilyMember) {
        state.update { list -> list.map { if (it.id == member.id) member else it } }
        changed()
    }

    override fun delete(id: Long) {
        state.update { list -> list.filterNot { it.id == id } }
        changed()
    }

    override fun deleteAll() {
        state.value = emptyList()
        changed()
    }

    /** 바뀔 때마다 불린다. 기기에 저장하는 구현이 덮어쓴다. */
    protected open fun changed() {}
}

/** 기기에 남는 가족 목록. 목소리 특징도 이 기기에만 저장한다 (서버에는 남지 않는다). */
class PrefsFamilyRepository(context: Context) : InMemoryFamilyRepository(load(context)) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun changed() {
        prefs.edit().putString(KEY_MEMBERS, toJson(members.value).toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "family"
        private const val KEY_MEMBERS = "members"

        private fun load(context: Context): List<FamilyMember> {
            val text = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_MEMBERS, null) ?: return emptyList()
            return try {
                val arr = JSONArray(text)
                (0 until arr.length()).mapNotNull { i ->
                    val o = arr.optJSONObject(i) ?: return@mapNotNull null
                    val instrument = Instrument.entries.firstOrNull { it.name == o.optString("instrument") } ?: return@mapNotNull null
                    val emb = o.optJSONArray("embedding")?.let { e -> (0 until e.length()).map { e.optDouble(it).toFloat() } }
                    FamilyMember(o.optLong("id"), o.optString("name"), instrument, o.optBoolean("hasVoice"), emb)
                }
            } catch (_: JSONException) {
                emptyList()
            }
        }

        private fun toJson(list: List<FamilyMember>) = JSONArray(
            list.map { m ->
                JSONObject()
                    .put("id", m.id)
                    .put("name", m.name)
                    .put("instrument", m.instrument.name)
                    .put("hasVoice", m.hasVoice)
                    .put("embedding", m.voiceEmbedding?.let { e -> JSONArray(e.map { it.toDouble() }) } ?: JSONObject.NULL)
            },
        )
    }
}
