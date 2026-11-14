package com.hmgappchallenge.kidsrhythm.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteTargetsLabelTest {
    @Test
    fun `두 곡이면 이름을 모두 적고 마지막 이름에 맞춰 조사를 붙인다`() {
        assertEquals("'둥둥 노래', '기차 노래'가", deleteTargetsLabel(listOf("둥둥 노래", "기차 노래")))
    }

    @Test
    fun `받침으로 끝나는 제목에는 이를 붙인다`() {
        assertEquals("'우리 가족 산책'이", deleteTargetsLabel(listOf("우리 가족 산책")))
    }

    @Test
    fun `세 곡 이상이면 두 곡만 적고 나머지는 외 N곡으로 줄인다`() {
        assertEquals("'가 노래', '나 노래' 외 2곡이", deleteTargetsLabel(listOf("가 노래", "나 노래", "다 노래", "라 노래")))
    }
}
