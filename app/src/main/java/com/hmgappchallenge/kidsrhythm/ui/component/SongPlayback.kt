package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.completed.formatPlayTime
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val LyricsShape = RoundedCornerShape(32.dp)
private val ProgressShape = RoundedCornerShape(8.dp)
private val MiniTrackShape = RoundedCornerShape(6.dp)
private val MiniPlayerShape = RoundedCornerShape(32.dp)
private val TileShape = RoundedCornerShape(20.dp)
private val MiniSubtitleStyle = AppTypography.LabelMedium.copy(fontSize = 22.sp)

/** 가사 줄에서 [word]가 나오는 곳을 모두 주황으로 칠한다. */
fun highlightWord(line: String, word: String?): AnnotatedString = buildAnnotatedString {
    append(line)
    if (!word.isNullOrBlank()) {
        var from = line.indexOf(word)
        while (from >= 0) {
            addStyle(SpanStyle(color = AppColors.BrandOrange), from, from + word.length)
            from = line.indexOf(word, from + word.length)
        }
    }
}

/**
 * 가사 전체. 지금 부르는 줄([current])만 진하게, 나머지는 옅게 보여 준다. 벌스·후렴이 바뀌는 곳은 한 줄 띄운다.
 * [activeText]로 지금 줄의 강조 글자를 정하고, 줄을 누르면 [onLineClick]으로 그 줄 번호를 알린다.
 */
@Composable
fun SongLyricsList(
    lines: List<String>,
    current: Int,
    modifier: Modifier = Modifier,
    sections: List<String> = emptyList(),
    activeText: (Int) -> AnnotatedString = { AnnotatedString(lines[it]) },
    onLineClick: ((Int) -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(current) {
        if (lines.isNotEmpty()) listState.animateScrollToItem((current - 1).coerceAtLeast(0))
    }
    LazyColumn(
        state = listState,
        modifier = modifier
            .clip(LyricsShape)
            .background(AppColors.SurfaceBasic),
        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        itemsIndexed(lines) { index, line ->
            val section = sections.getOrNull(index)
            if (index > 0 && section != null && section != sections.getOrNull(index - 1)) Spacer(Modifier.height(30.dp))
            val active = index == current
            Text(
                text = if (active) activeText(index) else AnnotatedString(line),
                style = if (active) AppTypography.TitleLargeExtraStrong else AppTypography.TitleLargeNormal,
                color = if (active) AppColors.TextDarkPrimary else AppColors.RecordingTime,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (onLineClick != null) Modifier.clickable(role = Role.Button) { onLineClick(index) } else Modifier),
            )
        }
    }
}

/** 재생 막대: 재생·멈춤 버튼, 눌러서 옮기는 진행 막대, "0:39 / 1:42". [onNext]가 있으면 오른쪽에 다음 곡 버튼을 둔다. */
@Composable
fun SongProgressPlayer(
    isPlaying: Boolean,
    positionMillis: Long,
    durationMillis: Long,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
        RoundIconButton(
            icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            contentDescription = if (isPlaying) "멈추기" else "재생",
            onClick = onTogglePlay,
            size = 100.dp,
            iconSize = 52.dp,
            background = AppColors.BasicPlayButton,
            tint = AppColors.BasicStaticLight100,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val fraction = if (durationMillis > 0) (positionMillis.toFloat() / durationMillis).coerceIn(0f, 1f) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(ProgressShape)
                    .background(AppColors.BasicProgressTrack)
                    .pointerInput(durationMillis) {
                        detectTapGestures { offset -> if (durationMillis > 0) onSeek((offset.x / size.width * durationMillis).toLong()) }
                    },
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(ProgressShape)
                        .background(AppColors.BrandOrange),
                )
            }
            Text("${formatPlayTime(positionMillis)} / ${formatPlayTime(durationMillis)}", style = AppTypography.LabelMedium, color = AppColors.RecordingTime)
        }
        if (onNext != null) {
            RoundIconButton(
                icon = R.drawable.ic_next,
                contentDescription = "다음 노래",
                onClick = onNext,
                size = 80.dp,
                iconSize = 44.dp,
                background = AppColors.BasicAlphaLight50,
                tint = AppColors.TextDarkPrimary,
            )
        }
    }
}

/**
 * 화면 아래 미니 플레이어. 제목과 진행 막대, 멈춤·다음 곡 버튼.
 * [tone]이 있으면 왼쪽에 음표 타일을(보관함), [subtitle]이 있으면 제목 옆에 가족·악기를(다시 듣기) 보여 준다.
 */
@Composable
fun SongMiniPlayer(
    title: String,
    progress: Float,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    tone: SongTone? = null,
    subtitle: String? = null,
    buttonSize: Dp = 80.dp,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .dropShadow(Color.Black.copy(alpha = 0.08f), 6.dp, 24.dp, 32.dp)
            .clip(MiniPlayerShape)
            .background(AppColors.SurfaceBasic)
            .border(2.dp, AppColors.SpeedChipBackground, MiniPlayerShape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (tone != null) SongToneTile(tone)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = AppTypography.BodyLargeStrong, color = AppColors.TextDarkPrimary, maxLines = 1)
                if (subtitle != null) Text(subtitle, style = MiniSubtitleStyle, color = AppColors.TextDarkTertiary, maxLines = 1)
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(MiniTrackShape)
                    .background(AppColors.SpeedChipBackground),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(MiniTrackShape)
                        .background(AppColors.BrandOrange),
                )
            }
        }
        val iconSize = buttonSize * 0.65f
        RoundIconButton(
            icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            contentDescription = if (isPlaying) "멈추기" else "재생",
            onClick = onTogglePlay,
            size = buttonSize,
            iconSize = iconSize,
            background = AppColors.SpeedChipBackground,
            tint = AppColors.TextDarkPrimary,
        )
        RoundIconButton(
            icon = R.drawable.ic_next,
            contentDescription = "다음 노래",
            onClick = onNext,
            size = buttonSize,
            iconSize = iconSize,
            background = AppColors.SpeedChipBackground,
            tint = AppColors.TextDarkPrimary,
        )
    }
}

/** 곡 썸네일: 둥근 사각 타일 위 음표. 곡마다 [tone] 색이 다르다. */
@Composable
fun SongToneTile(tone: SongTone) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(TileShape)
            .background(tone.background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(R.drawable.ic_music), contentDescription = null, tint = tone.icon, modifier = Modifier.size(52.dp))
    }
}

@Composable
private fun RoundIconButton(icon: Int, contentDescription: String, onClick: () -> Unit, size: Dp, iconSize: Dp, background: Color, tint: Color) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}
