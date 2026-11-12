package com.hmgappchallenge.kidsrhythm.ui.component

import com.hmgappchallenge.kidsrhythm.beat.SpeedZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MascotAnimationTest {
    @Test
    fun `부릉이는 8·16비트에서 생동감 춤, 2·4비트에서 귀 춤을 춘다`() {
        assertEquals(MascotAnimation.Lively, mascotAnimationFor(MascotCharacter.LION, SpeedZone.CITY))
        assertEquals(MascotAnimation.Lively, mascotAnimationFor(MascotCharacter.LION, SpeedZone.HIGHWAY))
        assertEquals(MascotAnimation.Ears, mascotAnimationFor(MascotCharacter.LION, SpeedZone.STOP))
        assertEquals(MascotAnimation.Ears, mascotAnimationFor(MascotCharacter.LION, SpeedZone.ALLEY))
    }

    @Test
    fun `애니메이션이 아직 없는 친구는 멈춘 이미지로 둔다`() {
        assertNull(mascotAnimationFor(MascotCharacter.CAT, SpeedZone.CITY))
    }
}
