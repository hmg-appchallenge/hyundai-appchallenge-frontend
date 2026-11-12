package com.hmgappchallenge.kidsrhythm.character

import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterStoreTest {
    @Test
    fun `저장된 친구가 없거나 모르는 이름이면 부릉이(사자)다`() {
        assertEquals(MascotCharacter.LION, characterOf(null))
        assertEquals(MascotCharacter.LION, characterOf("UNICORN"))
    }

    @Test
    fun `저장된 이름으로 친구를 찾는다`() {
        assertEquals(MascotCharacter.CAT, characterOf("CAT"))
    }

    @Test
    fun `친구 이름은 시안의 다섯 이름이다`() {
        assertEquals(listOf("붕붕이", "삐뽀", "두두", "빵빵이", "부릉이"), MascotCharacter.entries.map { it.nickname })
    }
}
