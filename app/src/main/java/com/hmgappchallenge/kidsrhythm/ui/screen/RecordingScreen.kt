package com.hmgappchallenge.kidsrhythm.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hmgappchallenge.kidsrhythm.AppServices
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.audio.BeatPlayer
import com.hmgappchallenge.kidsrhythm.beat.beatTypeForBpm
import com.hmgappchallenge.kidsrhythm.beat.bpmForSpeed
import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.recording.FakeRecording
import com.hmgappchallenge.kidsrhythm.recording.RecordingController
import com.hmgappchallenge.kidsrhythm.recording.RecordingSnapshot
import com.hmgappchallenge.kidsrhythm.recording.formatElapsed
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.LoadingSpinner
import com.hmgappchallenge.kidsrhythm.ui.component.MessagePopup
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.component.VoiceCard
import com.hmgappchallenge.kidsrhythm.ui.component.VoiceCardState
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlin.math.abs
import kotlinx.coroutines.launch

private val WaveformShape = RoundedCornerShape(32.dp)
private val ChipShape = RoundedCornerShape(16.dp)
private val BORDER_WIDTH = 2.dp
private val WaveformHeight = 190.dp
private val BarWidth = 12.dp
private val BarGap = 10.dp
private val PlayheadDotSize = 18.dp

/**
 * S04 녹음. 마이크로 녹음하면서 2초마다 서버로 보내고, 돌아온 결과로 연주 중인 가족과 들린 단어를 보여 준다.
 * 완성하기를 누르면 노래 작업 번호와 처음 들린 단어를 [onComplete]로 넘긴다.
 */
@Composable
fun RecordingRoute(
    family: List<FamilyMember>,
    services: AppServices,
    speedKmh: Float,
    outsideTemperature: Float?,
    onBack: () -> Unit,
    onComplete: (songId: String, firstWord: String?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = remember { RecordingController(services.songService, services.newRecorder(), services.sounds, scope, services.appScope) }
    val currentSpeed by rememberUpdatedState(speedKmh)
    val currentTemperature by rememberUpdatedState(outsideTemperature)
    controller.vehicle = { currentSpeed to currentTemperature }

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        permissionDenied = !granted
    }
    LaunchedEffect(hasPermission) {
        if (hasPermission) controller.start(family) else if (!permissionDenied) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    // 녹음하는 동안 비트는 작게 (마이크로 들어가지 않게)
    DisposableEffect(Unit) {
        services.beatPlayer.playForSpeed(speedKmh)
        services.beatPlayer.setVolume(BeatPlayer.RECORDING_VOLUME)
        onDispose { services.beatPlayer.setVolume(BeatPlayer.DEFAULT_VOLUME) }
    }
    // 백그라운드로 가면 녹음을 멈춘다. 돌아와서는 사용자가 계속하기로 다시 시작한다
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) controller.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // 완성하지 않고 화면을 떠나면 서버에 모아 둔 녹음을 지운다
    DisposableEffect(controller) { onDispose { controller.cancel() } }

    // 매 프레임 시간을 흘려 파형이 부드럽게 밀린다
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    val ticking = controller.phase == RecordingController.Phase.Recording && !controller.isPaused
    LaunchedEffect(ticking) {
        if (!ticking) {
            // 멈추면 실제로 녹음된 시간으로 맞춘다 (3분을 다 쓰면 02:59가 아니라 03:00)
            elapsedMillis = controller.recordedMillis
            return@LaunchedEffect
        }
        var last = withFrameMillis { it }
        while (true) {
            withFrameMillis { now ->
                val next = elapsedMillis + (now - last)
                // 실제로 녹음된 시간과 0.8초 넘게 어긋나면 맞춘다 (다시 녹음해서 0초부터 시작할 때 등)
                elapsedMillis = if (abs(next - controller.recordedMillis) > 800) controller.recordedMillis else next
                last = now
            }
        }
    }
    LaunchedEffect(controller.phase) {
        if (controller.phase == RecordingController.Phase.Connecting) elapsedMillis = 0L
    }
    val completeWith: (String?) -> Unit = { songId -> if (songId != null) onComplete(songId, controller.heardWords.firstOrNull()) }
    val recording = controller.phase == RecordingController.Phase.Recording
    RecordingScreen(
        family = family,
        snapshot = controller.snapshot(elapsedMillis),
        isPaused = controller.isPaused || !recording,
        canResume = !controller.isAtLimit,
        statusNote = controller.warning,
        onBack = onBack,
        onComplete = {
            if (recording) {
                scope.launch {
                    val bpm = bpmForSpeed(currentSpeed)
                    completeWith(controller.complete(bpm, beatTypeForBpm(bpm)))
                }
            }
        },
        onTogglePause = controller::togglePause,
        overlay = {
            when {
                permissionDenied -> MessagePopup(
                    title = "마이크 권한이 필요해요",
                    message = "노래를 만들려면 가족 목소리를 녹음해야 해요. 마이크 사용을 허용해 주세요.",
                    primaryText = "다시 요청하기",
                    onPrimary = {
                        permissionDenied = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    secondaryText = "나가기",
                    onSecondary = onBack,
                )
                // 완성하기 요청만 실패했으면 녹음을 버리지 않고 다시 보낸다
                controller.phase == RecordingController.Phase.Failed && controller.canRetryFinish -> MessagePopup(
                    title = "노래를 아직 보내지 못했어요",
                    message = controller.errorMessage ?: "잠시 후 다시 완성해 주세요.",
                    primaryText = "다시 완성하기",
                    onPrimary = { scope.launch { completeWith(controller.retryFinish()) } },
                    secondaryText = "나가기",
                    onSecondary = onBack,
                )
                controller.phase == RecordingController.Phase.Failed -> MessagePopup(
                    title = "녹음을 이어 갈 수 없어요",
                    message = controller.errorMessage ?: "잠시 후 다시 시도해 주세요.",
                    primaryText = "다시 시도",
                    onPrimary = { controller.start(family) },
                    secondaryText = "나가기",
                    onSecondary = onBack,
                )
                controller.phase == RecordingController.Phase.Connecting -> BusyChip("노래 서버에 연결하는 중이에요")
                controller.phase == RecordingController.Phase.Finishing -> BusyChip("녹음을 마무리하는 중이에요")
            }
        },
    )
}

/** 화면 가운데 위쪽에 잠깐 띄우는 진행 안내. */
@Composable
private fun BoxScope.BusyChip(text: String) {
    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 40.dp)
            .clip(CircleShape)
            .background(AppColors.SurfaceBasic)
            .border(BORDER_WIDTH, AppColors.BasicAlphaLight100, CircleShape)
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LoadingSpinner(size = 40.dp, contentDescription = null)
        Text(text = text, style = AppTypography.BodySmallStrong, color = AppColors.TextDarkSecondary)
    }
}

@Composable
fun RecordingScreen(
    family: List<FamilyMember>,
    snapshot: RecordingSnapshot,
    isPaused: Boolean,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onTogglePause: () -> Unit,
    /** 녹음할 수 있는 시간이 남았는지. 다 쓰면 계속하기를 막고 완성하기만 남긴다. */
    canResume: Boolean = true,
    statusNote: String? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    SubPage(title = "같이 노래해요", onBack = onBack, overlay = overlay) {
        Column(modifier = Modifier.weight(1f)) {
            RecordingStatus(elapsedMillis = snapshot.elapsedMillis, isPaused = isPaused, note = statusNote)
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
                enabled = !isPaused || canResume,
                icon = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause,
                variant = LargeButtonVariant.Secondary,
                modifier = Modifier.width(599.dp),
            )
        }
    }
}

@Composable
private fun RecordingStatus(elapsedMillis: Long, isPaused: Boolean, note: String?) {
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
        if (note != null) {
            Spacer(Modifier.width(24.dp))
            Text(text = note, style = AppTypography.BodySmallStrong, color = AppColors.InformativeNegative)
        }
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
