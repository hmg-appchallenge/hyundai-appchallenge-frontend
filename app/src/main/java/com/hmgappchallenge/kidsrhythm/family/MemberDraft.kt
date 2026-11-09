package com.hmgappchallenge.kidsrhythm.family

import com.hmgappchallenge.kidsrhythm.ui.component.Instrument

/** 이름 최대 글자 수. TODO(기획): 확인 중인 값이다 */
const val MAX_NAME_LENGTH = 8

/** 목소리 한 번 녹음하는 길이. TODO(기획): 확인 중인 값이다 (시안의 "2초 남았어요"를 보고 3초로 둠) */
const val VOICE_SAMPLE_MILLIS = 3_000L

sealed interface VoiceStatus {
    data object None : VoiceStatus
    data class Recording(val remainingMillis: Long) : VoiceStatus
    data object Registered : VoiceStatus
}

/** 추가·수정 팝업에서 입력 중인 가족 정보. */
data class MemberDraft(
    val name: String = "",
    val instrument: Instrument? = null,
    val voice: VoiceStatus = VoiceStatus.None,
) {
    /** 이름과 악기가 있으면 저장할 수 있다. 목소리는 나중에 등록해도 된다. */
    val canSubmit: Boolean get() = name.isNotBlank() && instrument != null

    fun withName(raw: String): MemberDraft = copy(name = raw.take(MAX_NAME_LENGTH))

    fun startRecording(): MemberDraft = copy(voice = VoiceStatus.Recording(VOICE_SAMPLE_MILLIS))

    /** 녹음 시간을 흘린다. 다 지나면 등록됨으로 바뀐다. */
    fun tickRecording(elapsedMillis: Long): MemberDraft {
        val recording = voice as? VoiceStatus.Recording ?: return this
        val remaining = recording.remainingMillis - elapsedMillis
        return copy(voice = if (remaining <= 0) VoiceStatus.Registered else VoiceStatus.Recording(remaining))
    }

    companion object {
        fun from(member: FamilyMember) = MemberDraft(
            name = member.name,
            instrument = member.instrument,
            voice = if (member.hasVoice) VoiceStatus.Registered else VoiceStatus.None,
        )
    }
}

/** 다른 가족이 이미 쓰는 악기와 그 가족 이름. 한 악기는 한 사람만 쓴다. */
fun takenInstruments(members: List<FamilyMember>, editingId: Long?): Map<Instrument, String> =
    members.filter { it.id != editingId }.associate { it.instrument to it.name }
