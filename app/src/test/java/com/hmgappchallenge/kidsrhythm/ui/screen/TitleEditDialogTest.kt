package com.hmgappchallenge.kidsrhythm.ui.screen

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TitleEditDialogTest {
    @Test
    fun `제목은 공백 포함 12자까지 저장할 수 있다`() {
        assertTrue(isSavableTitle("사과 노래"))
        assertTrue(isSavableTitle("가".repeat(12)))
        assertFalse(isSavableTitle("가".repeat(13)))
    }

    @Test
    fun `비었거나 공백뿐인 제목은 저장할 수 없다`() {
        assertFalse(isSavableTitle(""))
        assertFalse(isSavableTitle("   "))
    }
}
