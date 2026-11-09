package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.recording.FakeRecording
import com.hmgappchallenge.kidsrhythm.recording.RecordingSnapshot
import com.hmgappchallenge.kidsrhythm.recording.formatElapsed
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.component.VoiceCard
import com.hmgappchallenge.kidsrhythm.ui.component.VoiceCardState
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val WaveformShape = RoundedCornerShape(32.dp)
private val ChipShape = RoundedCornerShape(16.dp)
private val BORDER_WIDTH = 2.dp
private val WaveformHeight = 190.dp
private val BarWidth = 12.dp
private val BarGap = 10.dp
private val PlayheadDotSize = 18.dp

/** S04 녹음. 경과 시간을 들고 있다가 멈추기를 누르면 시간을 멈춘다. */
@Composable
fun RecordingRoute(onBack: () -> Unit, onComplete: () -> Unit) {
    var elapsedMillis by rememberSaveable { mutableLongStateOf(0L) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    // 백그라운드로 가면 녹음을 멈춘다. 돌아와서는 사용자가 계속하기로 다시 시작한다
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) isPaused = true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // TODO(오디오): VoiceInput으로 실제 마이크 녹음을 시작·일시정지하고, 완성하기에서 녹음 파일을 넘긴다
    // 매 프레임 시간을 흘려 파형이 부드럽게 밀린다. 화면이 그려지지 않는 동안에는 프레임도 멈춘다
    LaunchedEffect(isPaused) {
        if (isPaused) return@LaunchedEffect
        var last = withFrameMillis { it }
        while (true) {
            withFrameMillis { now ->
                elapsedMillis += now - last
                last = now
            }
        }
    }
    val snapshot = remember(elapsedMillis) { FakeRecording.snapshot(elapsedMillis) }
    RecordingScreen(
        family = FakeRecording.family,
        snapshot = snapshot,
        isPaused = isPaused,
        onBack = onBack,
        onComplete = onComplete,
        onTogglePause = { isPaused = !isPaused },
    )
}

@Composable
fun RecordingScreen(
    family: List<FamilyMember>,
    snapshot: RecordingSnapshot,
    isPaused: Boolean,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onTogglePause: () -> Unit,
) {
    SubPage(title = "같이 노래해요", onBack = onBack) {
        Column(modifier = Modifier.weight(1f)) {
            RecordingStatus(elapsedMillis = snapshot.elapsedMillis, isPaused = isPaused)
            Waveform(
                levelHeights = snapshot.levelHeights,
                // 다음 막대가 붙기까지 얼마나 왔는지 (0~1)
                progressToNextBar = (snapshot.elapsedMillis % FakeRecording.BAR_INTERVAL_MILLIS) / FakeRecording.BAR_INTERVAL_MILLIS.toFloat(),
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                text = "가족 목소리 · 소리 낸 사람의 악기가 울려요",
                style = AppTypography.BodySmallStrong,
                color = AppColors.TextDarkSecondary,
                modifier = Modifier.padding(top = 47.dp, bottom = 16.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                family.forEach { member ->
                    VoiceCard(
                        name = member.name,
                        instrument = member.instrument,
                        // 멈춘 동안에는 아무도 소리를 내지 않으므로 모두 대기
                        state = if (!isPaused && member.id in snapshot.playingMemberIds) VoiceCardState.Playing else VoiceCardState.Waiting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            HeardWords(words = snapshot.heardWords, modifier = Modifier.padding(top = 51.dp))
            Spacer(Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            LargeButton(
                text = "완성하기",
                onClick = onComplete,
                icon = R.drawable.ic_check,
                modifier = Modifier.weight(1f),
            )
            LargeButton(
                text = if (isPaused) "계속하기" else "멈추기",
                onClick = onTogglePause,
                icon = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause,
                variant = LargeButtonVariant.Secondary,
                modifier = Modifier.width(599.dp),
            )
        }
    }
}

@Composable
private fun RecordingStatus(elapsedMillis: Long, isPaused: Boolean) {
    Row(
        modifier = Modifier.height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .background(if (isPaused) AppColors.IconDarkQuaternary else AppColors.InformativeNegative, CircleShape),
        )
        Text(text = if (isPaused) "잠시 멈춤" else "녹음 중", style = AppTypography.TitleLarge, color = AppColors.TextDarkPrimary)
        Text(text = formatElapsed(elapsedMillis), style = AppTypography.TitleLargeNormal, color = AppColors.TextDarkTertiary)
    }
}

/**
 * 녹음 파형. 새 막대가 오른쪽 끝에 붙고 오래된 막대는 왼쪽으로 밀려난다.
 * 막대가 다 차기 전에는 왼쪽 빈 곳을 옅은 선으로 채운다. 가운데 세로선은 재생 위치 표시다.
 */
@Composable
private fun Waveform(levelHeights: List<Int>, progressToNextBar: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(WaveformHeight),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(WaveformShape)
                .background(AppColors.SurfaceBasic)
                .border(BORDER_WIDTH, AppColors.ButtonBasicDisabled, WaveformShape)
                // 시안: 왼쪽 여백 32 + 테두리 2, 오른쪽 끝 막대는 테두리 안쪽 6 앞에서 끝난다
                .padding(start = 32.dp + BORDER_WIDTH, end = 6.dp + BORDER_WIDTH),
        ) {
            val barWidth = BarWidth.toPx()
            val pitch = barWidth + BarGap.toPx()
            val radius = CornerRadius(6.dp.toPx())
            // 미끄러지는 동안 양 끝에 반쯤 걸친 막대까지 그린다
            val slots = ((size.width + BarGap.toPx()) / pitch).toInt() + 2
            val visible = levelHeights.takeLast(slots)
            // 가장 최근 막대는 오른쪽 바깥에서 들어와 다음 막대가 붙을 때 오른쪽 끝에 닿는다
            val slide = (1f - progressToNextBar) * pitch
            val startX = size.width - visible.size * pitch + BarGap.toPx() + slide
            val centerY = size.height / 2
            // 미끄러지며 패딩 영역으로 넘어간 막대는 잘라낸다
            clipRect {
                if (startX > BarGap.toPx()) {
                    val lineHeight = 4.dp.toPx()
                    drawRoundRect(
                        color = AppColors.InformativeNegative.copy(alpha = 0.3f),
                        topLeft = Offset(0f, centerY - lineHeight / 2),
                        size = Size((startX - BarGap.toPx()).coerceAtLeast(0f), lineHeight),
                        cornerRadius = radius,
                    )
                }
                visible.forEachIndexed { index, heightDp ->
                    val h = heightDp.dp.toPx()
                    drawRoundRect(
                        color = AppColors.InformativeNegative,
                        topLeft = Offset(startX + index * pitch, centerY - h / 2),
                        size = Size(barWidth, h),
                        cornerRadius = radius,
                    )
                }
            }
        }
        // 재생 위치: 카드 위아래로 점이 걸친다
        Canvas(Modifier.fillMaxSize()) {
            val x = size.width / 2
            val dotRadius = PlayheadDotSize.toPx() / 2
            drawLine(AppColors.PlayheadLine, Offset(x, 0f), Offset(x, size.height), strokeWidth = BORDER_WIDTH.toPx())
            drawCircle(AppColors.PlayheadDot, dotRadius, Offset(x, -dotRadius))
            drawCircle(AppColors.PlayheadDot, dotRadius, Offset(x, size.height + dotRadius))
        }
    }
}

@Composable
private fun HeardWords(words: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "지금까지 들린 단어", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
        Row(
            modifier = Modifier
                .height(64.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            words.forEach { word ->
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .clip(ChipShape)
                        .background(AppColors.SurfaceBasic)
                        .border(BORDER_WIDTH, AppColors.BrandOrange, ChipShape)
                        .padding(horizontal = 24.dp + BORDER_WIDTH),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = word, style = AppTypography.BodySmallStrong, color = AppColors.TextDarkPrimary)
                }
            }
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun RecordingScreenPreview() {
    RecordingScreen(
        family = FakeRecording.family,
        snapshot = FakeRecording.snapshot(8_400),
        isPaused = false,
        onBack = {},
        onComplete = {},
        onTogglePause = {},
    )
}
