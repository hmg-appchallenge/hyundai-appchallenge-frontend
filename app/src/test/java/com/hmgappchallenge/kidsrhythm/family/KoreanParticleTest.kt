package com.hmgappchallenge.kidsrhythm.family

import org.junit.Assert.assertEquals
import org.junit.Test

class KoreanParticleTest {
    @Test
    fun `멤버 삭제 안내는 받침에 따라 을·를을 붙인다`() {
        assertEquals("엄마를 삭제했어요", memberDeletedMessage("엄마"))
        assertEquals("민준을 삭제했어요", memberDeletedMessage("민준"))
        assertEquals("첫째를 삭제했어요", memberDeletedMessage("첫째"))
    }

    @Test
    fun `한글로 끝나지 않는 이름은 를을 붙인다`() {
        assertEquals("Mom를 삭제했어요", memberDeletedMessage("Mom"))
    }
}
