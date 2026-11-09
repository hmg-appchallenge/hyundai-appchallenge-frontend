package com.hmgappchallenge.kidsrhythm.family

import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** 노래에 함께하는 가족 목록. 목소리 설정(S02-1~3)에서 바꾸고, 비트·녹음 화면에서 읽는다. */
interface FamilyRepository {
    val members: StateFlow<List<FamilyMember>>

    /** 자리가 없으면(최대 [MAX_FAMILY_MEMBERS]명) 추가하지 않고 false. */
    fun add(name: String, instrument: Instrument): Boolean
    fun update(member: FamilyMember)
    fun delete(id: Long)
}

const val MAX_FAMILY_MEMBERS = 4

/**
 * 앱을 끄면 사라지는 메모리 저장소.
 * TODO(AI 서버): 목소리 등록 데이터의 저장 위치(기기·서버)가 정해지면 바꾼다
 */
class InMemoryFamilyRepository(initial: List<FamilyMember> = FakeFamily.members) : FamilyRepository {
    private val state = MutableStateFlow(initial.take(MAX_FAMILY_MEMBERS))
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    override val members: StateFlow<List<FamilyMember>> = state

    override fun add(name: String, instrument: Instrument): Boolean {
        if (state.value.size >= MAX_FAMILY_MEMBERS) return false
        state.update { it + FamilyMember(nextId++, name, instrument) }
        return true
    }

    override fun update(member: FamilyMember) {
        state.update { list -> list.map { if (it.id == member.id) member else it } }
    }

    override fun delete(id: Long) {
        state.update { list -> list.filterNot { it.id == id } }
    }
}
