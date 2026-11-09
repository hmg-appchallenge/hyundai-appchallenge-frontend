package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.hmgappchallenge.kidsrhythm.server.SongGenerator
import com.hmgappchallenge.kidsrhythm.server.SongRequest
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.ScreenSkeleton
import java.io.File

// 화면 뼈대다. UI 시안이 나오면 화면별 파일로 나눠 구현한다.

@Composable
fun BaseBeatScreen(onStartRecording: () -> Unit) {
    ScreenSkeleton(
        screenId = "S03",
        title = "차량 베이스 비트",
        description = "BPM · 속도/패턴/계절 칩, 8스텝 비트 바",
    ) {
        LargeButton(text = "녹음 시작", onClick = onStartRecording)
    }
}

@Composable
fun GeneratingScreen(songGenerator: SongGenerator, onGenerated: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // 녹음과 비트 엔진을 구현하면 실제 값으로 바꾼다.
        songGenerator.generate(
            SongRequest(recording = File(context.cacheDir, "recording.pcm"), bpm = 80),
        )
        onGenerated()
    }
    ScreenSkeleton(
        screenId = "S05",
        title = "노래 만드는 중",
        description = "Tension 로더, 메인 비트 계속 재생",
    )
}

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
