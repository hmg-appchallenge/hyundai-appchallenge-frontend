package com.hmgappchallenge.kidsrhythm.recording

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeRecordingTest {
    @Test
    fun `녹음 시간은 분과 초 두 자리로 보여 준다`() {
        assertEquals("00:00", formatElapsed(0))
        assertEquals("01:24", formatElapsed(84_000))
        assertEquals("10:05", formatElapsed(605_900))
    }

    @Test
    fun `파형 막대는 녹음 시간에 맞춰 0_1초마다 하나씩 쌓인다`() {
        assertTrue(FakeRecording.snapshot(0).levelHeights.isEmpty())
        assertEquals(84, FakeRecording.snapshot(8_400).levelHeights.size)
    }

    @Test
    fun `오래 녹음해도 화면에 그릴 최근 막대만 남기고 패턴은 이어진다`() {
        val longRun = FakeRecording.snapshot(600_000).levelHeights
        assertEquals(200, longRun.size)
        // 막대 하나가 더 쌓여도 겹치는 구간은 그대로다 (패턴이 절대 순번 기준으로 이어진다)
        val next = FakeRecording.snapshot(600_100).levelHeights
        assertEquals(longRun.drop(1), next.dropLast(1))
    }

    @Test
    fun `들린 단어는 시간이 지나며 늘어나고 샘플 수를 넘지 않는다`() {
        assertTrue(FakeRecording.snapshot(2_999).heardWords.isEmpty())
        assertEquals(listOf("엄마"), FakeRecording.snapshot(3_000).heardWords)
        assertEquals(5, FakeRecording.snapshot(600_000).heardWords.size)
    }

    @Test
    fun `연주 중인 가족은 가족 목록에 있는 사람만 나온다`() {
        val ids = FakeRecording.family.map { it.id }.toSet()
        (0L..20_000L step 1_000L).forEach { t ->
            assertTrue(ids.containsAll(FakeRecording.snapshot(t).playingMemberIds))
        }
    }

    @Test
    fun `같은 시간이면 항상 같은 화면 상태가 나온다`() {
        assertEquals(FakeRecording.snapshot(12_345), FakeRecording.snapshot(12_345))
    }
}
