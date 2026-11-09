package com.hmgappchallenge.kidsrhythm.family

import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyRepositoryTest {
    @Test
    fun `가족은 최대 4명까지 추가하고 다 차면 더 받지 않는다`() {
        val repo = InMemoryFamilyRepository(initial = emptyList())
        repeat(MAX_FAMILY_MEMBERS) { assertTrue(repo.add("가족$it", Instrument.DRUM)) }
        assertFalse(repo.add("다섯째", Instrument.SHAKER))
        assertEquals(MAX_FAMILY_MEMBERS, repo.members.value.size)
    }

    @Test
    fun `새 가족은 기존과 겹치지 않는 번호를 받는다`() {
        val repo = InMemoryFamilyRepository(initial = FakeFamily.members)
        repo.add("넷째", Instrument.SHAKER)
        val ids = repo.members.value.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `가족 정보를 수정하면 같은 자리에서 바뀐다`() {
        val repo = InMemoryFamilyRepository(initial = FakeFamily.members)
        val first = repo.members.value.first()
        repo.update(first.copy(name = "엄마2", instrument = Instrument.TAMBOURINE))
        assertEquals(FamilyMember(first.id, "엄마2", Instrument.TAMBOURINE), repo.members.value.first())
    }

    @Test
    fun `가족을 삭제하면 목록에서 빠지고 빈 자리에 다시 추가할 수 있다`() {
        val repo = InMemoryFamilyRepository(initial = FakeFamily.members)
        repo.add("넷째", Instrument.DRUM)
        repo.delete(repo.members.value.first().id)
        assertEquals(3, repo.members.value.size)
        assertTrue(repo.add("새 가족", Instrument.SHAKER))
    }
}
