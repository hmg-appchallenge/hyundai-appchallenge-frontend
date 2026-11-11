package com.hmgappchallenge.kidsrhythm.ui.component

import com.hmgappchallenge.kidsrhythm.R
import org.junit.Assert.assertEquals
import org.junit.Test

class DesignAssetMappingTest {
    @Test
    fun `악기 12종은 모두 서로 다른 배지 아이콘과 큰 픽토그램이 있다`() {
        assertEquals(12, Instrument.entries.map { it.iconRes }.toSet().size)
        assertEquals(12, Instrument.entries.map { it.pictogramRes }.toSet().size)
    }

    @Test
    fun `악기 고르기에는 12종이 분류별 기획 순서대로 나오고 분류마다 3종씩 있다`() {
        assertEquals(
            listOf("북", "스틸드럼", "우드블록", "탬버린", "셰이커", "마라카스", "우쿨렐레", "기타", "하프", "벨", "실로폰", "피아노"),
            Instrument.entries.map { it.label },
        )
        InstrumentCategory.entries.forEach { category ->
            assertEquals(3, Instrument.entries.count { it.category == category })
        }
    }

    @Test
    fun `마스코트 5종은 서 있는 버전과 앉은 버전 이미지가 모두 서로 다르다`() {
        val images = MascotCharacter.entries.flatMap { c -> MascotPose.entries.map { c.imageRes(it) } }
        assertEquals(10, images.toSet().size)
    }

    @Test
    fun `포즈에 맞는 마스코트 이미지를 고른다`() {
        assertEquals(R.drawable.img_mascot_lion_standing, MascotCharacter.LION.imageRes(MascotPose.Standing))
        assertEquals(R.drawable.img_mascot_lion_sitting, MascotCharacter.LION.imageRes(MascotPose.Sitting))
    }

    @Test
    fun `목소리 카드 레벨 바는 20개다`() {
        assertEquals(20, PlayingLevelHeights.size)
    }
}
