package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import com.hmgappchallenge.kidsrhythm.ui.theme.AstaSans

/** 목소리 카드 상태. 녹음 전(Ready) 상태는 아직 정해지지 않았다. */
enum class VoiceCardState { Playing, Waiting }

private val CardCornerRadius = 32.dp
private val CardShape = RoundedCornerShape(CardCornerRadius)

/** 상태 배지 글자. 공용 타이포 토큰에 없는 크기라 여기서만 쓴다. */
private val StatusTextStyle = TextStyle(
    fontFamily = AstaSans,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
)

/** 연주 중일 때 레벨 바 높이. */
internal val PlayingLevelHeights = listOf(16, 32, 48, 28, 56, 40, 20, 44, 52, 24, 36, 48, 16, 40, 28, 20, 48, 32, 12, 24)
private const val WAITING_LEVEL_HEIGHT = 8

/** 가족 목소리별 카드. */
@Composable
fun VoiceCard(
    name: String,
    instrument: Instrument,
    state: VoiceCardState,
    modifier: Modifier = Modifier,
) {
    val playing = state == VoiceCardState.Playing
    // border는 공간을 차지하지 않으므로 패딩에 두께를 더해 테두리 바깥 크기를 맞춘다
    val borderWidth = 2.dp
    Column(
        // 기본 너비는 508. 호출하는 쪽이 modifier나 weight로 크기를 정하면 그 크기를 따른다
        modifier = modifier
            .width(508.dp)
            .then(if (playing) Modifier.dropShadow(AppColors.MusicPrimary.copy(alpha = 0.2f), 8.dp, 16.dp, CardCornerRadius) else Modifier)
            .clip(CardShape)
            .background(AppColors.SurfaceBasic)
            .border(
                width = borderWidth,
                color = if (playing) AppColors.BrandOrange else AppColors.BasicAlphaLight100,
                shape = CardShape,
            )
            .padding(28.dp + borderWidth),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 아바타: 120 배지를 96 원으로 자른다 (아이콘은 64 그대로)
            InstrumentBadge(
                instrument = instrument,
                modifier = Modifier.clip(CircleShape),
                size = 96.dp,
                // 120 배지 기준 아이콘 크기를 유지한다
                iconSize = instrument.iconSize,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = name, style = AppTypography.TitleLargeExtraStrong, color = AppColors.TextDarkPrimary, maxLines = 1)
                Text(text = instrument.label, style = AppTypography.BodySmallStrong, color = AppColors.TextDarkTertiary, maxLines = 1)
            }
            StatusPill(playing)
        }
        LevelBars(playing)
    }
}

@Composable
private fun StatusPill(playing: Boolean) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (playing) AppColors.BrandOrange else AppColors.BasicAlphaLight50)
            .padding(start = 16.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(if (playing) AppColors.BasicStaticLight100 else AppColors.IconDarkQuaternary, CircleShape),
        )
        Text(
            text = if (playing) "연주 중" else "대기",
            style = StatusTextStyle,
            color = if (playing) AppColors.BasicStaticLight100 else AppColors.TextDarkSecondary,
            maxLines = 1,
        )
    }
}

@Composable
private fun LevelBars(playing: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayingLevelHeights.forEach { h ->
            Box(
                Modifier
                    .weight(1f)
                    .height((if (playing) h else WAITING_LEVEL_HEIGHT).dp)
                    .background(
                        if (playing) AppColors.BrandOrange else AppColors.BasicAlphaLight200,
                        RoundedCornerShape(6.dp),
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun VoiceCardPlayingPreview() {
    VoiceCard(name = "엄마", instrument = Instrument.DRUM, state = VoiceCardState.Playing)
}

@Preview
@Composable
private fun VoiceCardWaitingPreview() {
    VoiceCard(name = "엄마", instrument = Instrument.DRUM, state = VoiceCardState.Waiting)
}
