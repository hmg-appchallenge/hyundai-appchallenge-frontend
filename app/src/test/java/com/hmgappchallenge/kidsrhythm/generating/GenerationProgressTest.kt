package com.hmgappchallenge.kidsrhythm.generating

import com.hmgappchallenge.kidsrhythm.generating.GenerationProgress.FINISH_STEP_MILLIS
import com.hmgappchallenge.kidsrhythm.generating.GenerationProgress.STEP_MILLIS
import com.hmgappchallenge.kidsrhythm.generating.GenerationProgress.doneCount
import com.hmgappchallenge.kidsrhythm.generating.GenerationProgress.doneCountForServer
import com.hmgappchallenge.kidsrhythm.generating.GenerationProgress.isComplete
import com.hmgappchallenge.kidsrhythm.generating.GenerationProgress.stateOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerationProgressTest {
    private val stepCount = GenerationProgress.steps.size

    @Test
    fun `처음에는 첫 단계가 진행 중이다`() {
        assertEquals(0, doneCount(elapsedMillis = 0, generatedAtMillis = null))
        assertEquals(StepState.Active, stateOf(0, doneCount = 0))
        assertEquals(StepState.Pending, stateOf(1, doneCount = 0))
    }

    @Test
    fun `결과 전에는 시간마다 한 단계씩 넘어간다`() {
        assertEquals(1, doneCount(STEP_MILLIS, null))
        assertEquals(2, doneCount(STEP_MILLIS * 2 + 1, null))
    }

    @Test
    fun `결과 전에는 마지막 단계에서 기다리고 끝내지 않는다`() {
        val done = doneCount(STEP_MILLIS * 100, null)
        assertEquals(stepCount - 1, done)
        assertFalse(isComplete(done))
        assertEquals(StepState.Active, stateOf(stepCount - 1, done))
    }

    @Test
    fun `결과가 오면 진행 중이던 단계가 바로 끝난다`() {
        val arrivedAt = STEP_MILLIS + 500
        assertEquals(2, doneCount(arrivedAt, arrivedAt))
    }

    @Test
    fun `결과가 오면 남은 단계를 빠르게 채워 모두 끝낸다`() {
        val arrivedAt = 500L
        assertEquals(1, doneCount(arrivedAt, arrivedAt))
        assertEquals(2, doneCount(arrivedAt + FINISH_STEP_MILLIS, arrivedAt))
        val done = doneCount(arrivedAt + FINISH_STEP_MILLIS * (stepCount - 1), arrivedAt)
        assertEquals(stepCount, done)
        assertTrue(isComplete(done))
    }

    @Test
    fun `끝난 단계 수는 단계 수를 넘지 않는다`() {
        assertEquals(stepCount, doneCount(STEP_MILLIS * 100, generatedAtMillis = 0))
    }

    @Test
    fun `화면 단계 이름은 서버 단계 이름과 같은 순서다`() {
        // 서버 server/config.py STEPS, docs/API.md 5번
        val serverSteps = listOf("FIND_WORDS", "WRITE_LYRICS", "SPLIT_VOICES", "ADD_INSTRUMENTS", "FINISH")
        assertEquals(serverSteps, GenerationProgress.steps.map { it.name })
    }

    @Test
    fun `서버가 알려 준 단계 앞까지 끝난 것으로 본다`() {
        assertEquals(0, doneCountForServer(step = null, status = "QUEUED"))
        assertEquals(0, doneCountForServer("FIND_WORDS", "RUNNING"))
        assertEquals(2, doneCountForServer("SPLIT_VOICES", "RUNNING"))
        assertEquals(stepCount - 1, doneCountForServer("FINISH", "RUNNING"))
    }

    @Test
    fun `서버가 완성이라고 하면 모든 단계가 끝난다`() {
        assertEquals(stepCount, doneCountForServer("FINISH", "DONE"))
        assertTrue(isComplete(doneCountForServer(step = null, status = "DONE")))
    }

    @Test
    fun `모르는 단계 이름이면 처음 단계부터 보여 준다`() {
        assertEquals(0, doneCountForServer("SOMETHING_NEW", "RUNNING"))
        assertFalse(isComplete(doneCountForServer("SOMETHING_NEW", "RUNNING")))
    }
}
