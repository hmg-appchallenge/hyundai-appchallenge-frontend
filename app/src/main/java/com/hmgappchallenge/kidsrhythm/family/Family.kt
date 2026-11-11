package com.hmgappchallenge.kidsrhythm.family

import com.hmgappchallenge.kidsrhythm.ui.component.Instrument

/** 노래에 함께하는 가족 한 명과 그 사람이 고른 악기. */
data class FamilyMember(
    val id: Long,
    val name: String,
    val instrument: Instrument,
    /** 목소리를 녹음해 두었는지. 나중에 등록해도 된다. */
    val hasVoice: Boolean = true,
    /** 서버가 계산해 준 목소리 특징. 기기에만 저장하고, 녹음할 때마다 서버에 같이 보낸다 (docs/API.md 2번). */
    val voiceEmbedding: List<Float>? = null,
)

/** 미리보기(@Preview)와 테스트에서 쓰는 가족. 앱은 S02-1에서 등록한 가족([PrefsFamilyRepository])을 쓴다. */
object FakeFamily {
    val members = listOf(
        FamilyMember(1, "엄마", Instrument.DRUM),
        FamilyMember(2, "첫째", Instrument.SHAKER),
        FamilyMember(3, "둘째", Instrument.TAMBOURINE),
    )
}
