package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.runtime.Composable
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.ScreenSkeleton

// 화면 뼈대다. UI 시안이 나오면 화면별 파일로 나눠 구현한다.

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
