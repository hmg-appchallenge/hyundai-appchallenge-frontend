package com.hmgappchallenge.kidsrhythm.family

private const val HANGUL_FIRST = '가'
private const val HANGUL_LAST = '힣'
private const val FINAL_CONSONANT_COUNT = 28

/** 이름 뒤에 붙는 목적격 조사. 받침이 있으면 "을", 없거나 한글이 아니면 "를"이다. */
fun objectParticle(name: String): String {
    val last = name.trimEnd().lastOrNull() ?: return "를"
    if (last !in HANGUL_FIRST..HANGUL_LAST) return "를"
    return if ((last - HANGUL_FIRST) % FINAL_CONSONANT_COUNT == 0) "를" else "을"
}

/** 멤버를 지운 뒤 보여 주는 안내. 예: "엄마를 삭제했어요" */
fun memberDeletedMessage(name: String): String = "$name${objectParticle(name)} 삭제했어요"
