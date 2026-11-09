package com.hmgappchallenge.kidsrhythm.family

import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemberDraftTest {
    @Test
    fun `이름은 최대 8자까지만 받는다`() {
        assertEquals("가나다라마바사아", MemberDraft().withName("가나다라마바사아자차").name)
    }

    @Test
    fun `이름과 악기가 있으면 목소리 없이도 추가할 수 있다`() {
        assertFalse(MemberDraft().canSubmit)
        assertFalse(MemberDraft(name = "엄마").canSubmit)
        assertFalse(MemberDraft(name = "  ", instrument = Instrument.DRUM).canSubmit)
        assertTrue(MemberDraft(name = "엄마", instrument = Instrument.DRUM).canSubmit)
    }

    @Test
    fun `목소리 녹음은 정해진 시간이 지나면 등록됨으로 바뀐다`() {
        val started = MemberDraft().startRecording()
        assertEquals(VoiceStatus.Recording(VOICE_SAMPLE_MILLIS), started.voice)
        assertEquals(VoiceStatus.Recording(1_000), started.tickRecording(VOICE_SAMPLE_MILLIS - 1_000).voice)
        assertEquals(VoiceStatus.Registered, started.tickRecording(VOICE_SAMPLE_MILLIS).voice)
    }

    @Test
    fun `수정 팝업은 기존 가족 정보로 채워진다`() {
        val member = FamilyMember(7, "첫째", Instrument.SHAKER, hasVoice = false)
        assertEquals(MemberDraft("첫째", Instrument.SHAKER, VoiceStatus.None), MemberDraft.from(member))
    }

    @Test
    fun `다른 가족이 쓰는 악기는 사용 중이고 수정 중인 본인 악기는 고를 수 있다`() {
        val members = listOf(
            FamilyMember(1, "엄마", Instrument.DRUM),
            FamilyMember(2, "첫째", Instrument.TAMBOURINE),
        )
        assertEquals(mapOf(Instrument.DRUM to "엄마", Instrument.TAMBOURINE to "첫째"), takenInstruments(members, editingId = null))
        assertEquals(mapOf(Instrument.TAMBOURINE to "첫째"), takenInstruments(members, editingId = 1))
    }
}
