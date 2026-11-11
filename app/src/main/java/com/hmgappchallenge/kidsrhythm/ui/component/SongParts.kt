package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.audio.SongPlayer
import com.hmgappchallenge.kidsrhythm.server.LyricLine
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private val BoxShape = RoundedCornerShape(32.dp)
private val LyricStyle = AppTypography.BodyLargeStrong.copy(fontWeight = FontWeight.Normal)

/** 재생 위치로 지금 부르는 가사 줄을 찾는다. 첫 줄 전이면 -1. */
fun currentLyricIndex(lines: List<LyricLine>, positionMs: Long): Int = lines.indexOfLast { it.startMs <= positionMs }

/** 1:42 같은 재생 시간. */
fun formatClock(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}

/** 가족 목소리에서 나온 단어(명사)는 주황으로 칠한다 (S06 시안의 '사과'). */
fun lyricText(line: LyricLine): AnnotatedString = buildAnnotatedString {
    append(line.text)
    line.highlights.filter { it.kind == "word" && it.start in 0..it.end && it.end <= line.text.length }.forEach {
        addStyle(SpanStyle(color = AppColors.BrandOrange, fontWeight = FontWeight.Bold), it.start, it.end)
    }
}

/**
 * 가사 상자. 지금 부르는 줄을 굵게, 나머지는 옅게 보여 주고, 줄을 누르면 그 부분부터 듣는다.
 * 섹션(벌스·후렴)이 바뀌는 곳에는 빈 줄을 둔다.
 */
@Composable
fun LyricsBox(lines: List<LyricLine>, positionMs: Long, onSeek: (Long) -> Unit, modifier: Modifier = Modifier) {
    val current = currentLyricIndex(lines, positionMs)
    val listState = rememberLazyListState()
    LaunchedEffect(current) {
        if (current >= 0) listState.animateScrollToItem((current - 1).coerceAtLeast(0))
    }
    Box(
        modifier = modifier
            .clip(BoxShape)
            .background(AppColors.SurfaceBasic)
            .padding(horizontal = 40.dp, vertical = 28.dp),
    ) {
        if (lines.isEmpty()) {
            Text("가사가 없는 노래예요", style = LyricStyle, color = AppColors.TextDarkTertiary)
        } else {
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                itemsIndexed(lines) { index, line ->
                    if (index > 0 && lines[index - 1].section != line.section) Spacer(Modifier.height(18.dp))
                    val active = index == current
                    Text(
                        text = lyricText(line),
                        style = if (active) AppTypography.BodyLargeStrong else LyricStyle,
                        color = if (active) AppColors.TextDarkPrimary else AppColors.TextDarkTertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { onSeek(line.startMs) }
                            .padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

/** 재생 중에는 프레임마다 재생 위치를 읽는다. */
@Composable
fun PlayerTicker(player: SongPlayer) {
    LaunchedEffect(player.isPlaying) {
        while (player.isPlaying) {
            withFrameMillis { player.tick() }
        }
    }
}

/** 재생 막대: 재생·멈춤 버튼, 진행 막대, 시간. [title]이 있으면 미니 플레이어 모양. */
@Composable
fun PlayerBar(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    onNext: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AppColors.ButtonFilledEnabled)
                .clickable(role = Role.Button, onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = if (isPlaying) "멈추기" else "재생",
                tint = AppColors.BasicStaticLight100,
                modifier = Modifier.size(52.dp),
            )
        }
        if (title != null) {
            Column(modifier = Modifier.weight(0.6f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = AppTypography.BodyLargeStrong, color = AppColors.TextDarkPrimary, maxLines = 1)
                if (subtitle != null) Text(subtitle, style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary, maxLines = 1)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProgressTrack(positionMs, durationMs, onSeek)
            Text("${formatClock(positionMs)} / ${formatClock(durationMs)}", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
        }
        if (onNext != null) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppColors.BasicAlphaLight50)
                    .clickable(role = Role.Button, onClick = onNext),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "다음 노래", tint = AppColors.IconDarkPrimary, modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun ProgressTrack(positionMs: Long, durationMs: Long, onSeek: (Long) -> Unit) {
    val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(durationMs) {
                detectTapGestures { offset -> if (durationMs > 0) onSeek((offset.x / size.width * durationMs).toLong()) }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(AppColors.BasicAlphaLight100),
        )
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(12.dp)
                .clip(CircleShape)
                .background(AppColors.BrandOrange),
        )
    }
}

/** 친구(마스코트) 이름. 피그마 '같이 놀 친구 고르기' 기준. */
val MascotCharacter.friendName: String
    get() = when (this) {
        MascotCharacter.DOG -> "둥둥이"
        MascotCharacter.CAT -> "콩콩이"
        MascotCharacter.FROG -> "말랑이"
        MascotCharacter.PIG -> "뭉게"
        MascotCharacter.LION -> "반짝이"
    }

/** 받침이 있으면 [withJong], 없으면 [withoutJong]을 붙인다 (콩콩이가, 뭉게가). */
fun String.josa(withJong: String, withoutJong: String): String {
    val c = lastOrNull() ?: return this
    val code = c.code - 0xAC00
    val hasJong = code in 0 until 11172 && code % 28 != 0
    return this + if (hasJong) withJong else withoutJong
}

/**
 * 노래에 맞춰 춤추는 친구. 박마다 통통 튀고 살짝 좌우로 흔든다.
 * 박 위치는 서버가 준 BPM과 첫 박 시간으로 계산한다 (docs/API.md 5번 firstBeatMs).
 */
@Composable
fun DancingMascot(
    character: MascotCharacter,
    positionMs: Long,
    bpm: Int,
    firstBeatMs: Long,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val beatMs = if (bpm > 0) 60_000.0 / bpm else 500.0
    val beats = ((positionMs - firstBeatMs).coerceAtLeast(0L) / beatMs)
    val phase = beats - kotlin.math.floor(beats)
    val bounce = if (isPlaying) abs(sin(PI * phase)).toFloat() else 0f
    val sway = if (isPlaying) sin(PI * beats).toFloat() else 0f
    Mascot(
        character = character,
        pose = if (isPlaying) MascotPose.Standing else MascotPose.Sitting,
        modifier = modifier.graphicsLayer {
            translationY = -bounce * 36.dp.toPx()
            rotationZ = sway * 5f
            val squash = 1f + 0.04f * (1f - bounce)
            transformOrigin = TransformOrigin(0.5f, 1f)
            scaleX = squash
            scaleY = 2f - squash
        },
    )
}
