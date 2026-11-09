package com.hmgappchallenge.kidsrhythm.ui.component

import com.hmgappchallenge.kidsrhythm.R
import org.junit.Assert.assertEquals
import org.junit.Test

class DesignAssetMappingTest {
    @Test
    fun `악기마다 배지 아이콘과 큰 픽토그램이 연결된다`() {
        assertEquals(
            listOf(R.drawable.ic_instrument_drum, R.drawable.ic_instrument_shaker, R.drawable.ic_instrument_tambourine),
            Instrument.entries.map { it.iconRes },
        )
        assertEquals(
            listOf(R.drawable.img_instrument_drum, R.drawable.img_instrument_shaker, R.drawable.img_instrument_tambourine),
            Instrument.entries.map { it.pictogramRes },
        )
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
