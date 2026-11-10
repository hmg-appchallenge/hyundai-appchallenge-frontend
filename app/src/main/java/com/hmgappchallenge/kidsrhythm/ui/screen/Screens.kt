package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.runtime.Composable
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.ScreenSkeleton

// 화면 뼈대다. UI 시안이 나오면 화면별 파일로 나눠 구현한다.

@Composable
fun CompletedScreen(onSave: () -> Unit, onMakeAnother: () -> Unit) {
    ScreenSkeleton(
        screenId = "S06",
        title = "완성",
        description = "재생 · 저장 · 한 곡 더",
    ) {
        LargeButton(text = "재생", onClick = {}, enabled = false)
        LargeButton(text = "보관함에 저장", onClick = onSave)
        LargeButton(text = "한 곡 더", onClick = onMakeAnother)
    }
}

@Composable
fun ArchiveScreen(onOpenMake: () -> Unit) {
    ScreenSkeleton(
        screenId = "S07",
        title = "보관함",
        description = "곡 목록 100dp 행, 미니 플레이어",
    ) {
        LargeButton(text = "만들기", onClick = onOpenMake)
    }
}
