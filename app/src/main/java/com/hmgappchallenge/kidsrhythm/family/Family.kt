package com.hmgappchallenge.kidsrhythm.family

import com.hmgappchallenge.kidsrhythm.ui.component.Instrument

/** 노래에 함께하는 가족 한 명과 그 사람이 고른 악기. */
data class FamilyMember(
    val id: Long,
    val name: String,
    val instrument: Instrument,
    /** 목소리를 녹음해 두었는지. 나중에 등록해도 된다. */
    val hasVoice: Boolean = true,
)

/** 가족 목소리 등록(S02-1)이 생기기 전까지 쓰는 가족. */
// TODO(가족 목소리): S02-1에서 등록한 가족과 악기로 바꾼다
object FakeFamily {
    val members = listOf(
        FamilyMember(1, "엄마", Instrument.DRUM),
        FamilyMember(2, "첫째", Instrument.SHAKER),
        FamilyMember(3, "둘째", Instrument.TAMBOURINE),
    )
}
