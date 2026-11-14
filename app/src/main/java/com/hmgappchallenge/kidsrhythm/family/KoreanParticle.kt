package com.hmgappchallenge.kidsrhythm.family

private const val HANGUL_FIRST = '가'
private const val HANGUL_LAST = '힣'
private const val FINAL_CONSONANT_COUNT = 28

/** 마지막 글자가 받침 있는 한글이면 true. 한글이 아니면 false로 본다. */
private fun hasFinalConsonant(name: String): Boolean {
    val last = name.trimEnd().lastOrNull() ?: return false
    if (last !in HANGUL_FIRST..HANGUL_LAST) return false
    return (last - HANGUL_FIRST) % FINAL_CONSONANT_COUNT != 0
}

/** 이름 뒤에 붙는 목적격 조사. 받침이 있으면 "을", 없거나 한글이 아니면 "를"이다. */
fun objectParticle(name: String): String = if (hasFinalConsonant(name)) "을" else "를"

/** 이름 뒤에 붙는 주격 조사. 받침이 있으면 "이", 없거나 한글이 아니면 "가"다. */
fun subjectParticle(name: String): String = if (hasFinalConsonant(name)) "이" else "가"

/** 친구 소개 말풍선. 예: "안녕! 나는 부릉이야!", "안녕! 나는 민준이야!" */
fun introduceMessage(name: String): String = "안녕! 나는 $name${if (hasFinalConsonant(name)) "이야" else "야"}!"

/** 멤버를 지운 뒤 보여 주는 안내. 예: "엄마를 삭제했어요" */
fun memberDeletedMessage(name: String): String = "$name${objectParticle(name)} 삭제했어요"
