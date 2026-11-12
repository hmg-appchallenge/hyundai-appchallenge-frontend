package com.hmgappchallenge.kidsrhythm.generating

/** 노래를 만드는 단계. 화면에 이 순서대로 보여 준다. */
enum class GenerationStep(val label: String) {
    FIND_WORDS("단어 찾기"),
    WRITE_LYRICS("노래 가사 만들기"),
    SPLIT_VOICES("목소리 나누기"),
    ADD_INSTRUMENTS("목소리마다 악기 입히기"),
    FINISH("노래 완성"),
}

enum class StepState { Done, Active, Pending }

/**
 * 단계 진행을 시간으로 흉내 낸다.
 * 결과가 오기 전에는 [STEP_MILLIS]마다 한 단계씩 넘기되 마지막 단계에서 기다리고,
 * 결과가 오면 남은 단계를 [FINISH_STEP_MILLIS]마다 하나씩 빠르게 채운다.
 * 서버와 연결된 화면은 서버가 알려 준 단계([doneCountForServer])를 쓰고, 시간 흉내([doneCount])는 미리보기용이다.
 */
object GenerationProgress {
    const val STEP_MILLIS = 2_000L
    const val FINISH_STEP_MILLIS = 300L

    /** 모든 단계를 채운 뒤 다음 화면으로 넘어가기 전에 잠깐 보여 주는 시간. */
    const val COMPLETE_HOLD_MILLIS = 600L

    val steps: List<GenerationStep> = GenerationStep.entries
    private val lastIndex = steps.lastIndex

    /**
     * 끝난 단계 수. [generatedAtMillis]는 결과가 도착한 시점이며, 아직 없으면 null이다.
     * 모두 끝나면 [steps]의 크기와 같다.
     */
    fun doneCount(elapsedMillis: Long, generatedAtMillis: Long?): Int {
        val byTime = (elapsedMillis / STEP_MILLIS).toInt().coerceAtMost(lastIndex)
        if (generatedAtMillis == null) return byTime
        // 결과가 오는 순간 진행 중이던 단계를 끝내고, 나머지를 하나씩 채운다
        val atArrival = (generatedAtMillis / STEP_MILLIS).toInt().coerceAtMost(lastIndex)
        val finishing = atArrival + 1 + ((elapsedMillis - generatedAtMillis) / FINISH_STEP_MILLIS).toInt()
        return maxOf(byTime, finishing).coerceAtMost(steps.size)
    }

    fun isComplete(doneCount: Int): Boolean = doneCount >= steps.size

    /**
     * 서버가 알려 준 단계로 끝난 단계 수를 정한다 (docs/API.md 5번).
     * 지금 단계 앞까지가 끝난 단계이고, 완성(DONE)이면 모두 끝난 것이다.
     */
    fun doneCountForServer(step: String?, status: String): Int {
        if (status == "DONE") return steps.size
        val index = steps.indexOfFirst { it.name == step }
        return if (index < 0) 0 else index
    }

    fun stateOf(index: Int, doneCount: Int): StepState = when {
        index < doneCount -> StepState.Done
        index == doneCount -> StepState.Active
        else -> StepState.Pending
    }
}
