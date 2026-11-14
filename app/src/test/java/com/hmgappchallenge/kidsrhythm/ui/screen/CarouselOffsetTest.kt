package com.hmgappchallenge.kidsrhythm.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class CarouselOffsetTest {
    private val widths = listOf(406f, 316f, 353f, 356f, 355f)
    private val gap = 8f

    /** 칸이 딱 맞을 때 [center]번이 가운데인 줄에서 이웃한 두 캐릭터 그림 사이. */
    private fun gapBetween(left: Int, right: Int, center: Int): Float {
        val leftSlot = carouselSlot(left, center.toFloat(), widths.size)
        val rightSlot = carouselSlot(right, center.toFloat(), widths.size)
        val leftEdge = carouselOffset(left, leftSlot, widths, gap) + widths[left] / 2
        val rightEdge = carouselOffset(right, rightSlot, widths, gap) - widths[right] / 2
        return rightEdge - leftEdge
    }

    @Test
    fun `가운데 캐릭터는 무대 가운데에 앉는다`() {
        assertEquals(0f, carouselOffset(2, 0f, widths, gap), 0.001f)
    }

    @Test
    fun `폭이 달라도 이웃한 캐릭터 그림 사이는 늘 8이다`() {
        for (center in widths.indices) {
            for (step in -2 until 2) {
                val left = Math.floorMod(center + step, widths.size)
                val right = Math.floorMod(center + step + 1, widths.size)
                assertEquals("center=$center left=$left", gap, gapBetween(left, right, center), 0.001f)
            }
        }
    }

    @Test
    fun `한 칸 넘어가는 중간에는 두 자리의 가운데에 있다`() {
        val from = carouselOffset(1, 1f, widths, gap)
        val to = carouselOffset(1, 0f, widths, gap)
        assertEquals((from + to) / 2, carouselOffset(1, 0.5f, widths, gap), 0.001f)
    }
}
