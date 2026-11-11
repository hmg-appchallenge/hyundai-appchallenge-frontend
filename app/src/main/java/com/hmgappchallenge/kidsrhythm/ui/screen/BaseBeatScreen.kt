package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.beat.beatTypeForSpeed
import com.hmgappchallenge.kidsrhythm.beat.bpmForSpeed
import com.hmgappchallenge.kidsrhythm.beat.speedBandLabel
import com.hmgappchallenge.kidsrhythm.family.FakeFamily
import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentOption
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.SettingBadge
import com.hmgappchallenge.kidsrhythm.ui.component.SettingRow
import com.hmgappchallenge.kidsrhythm.ui.component.StageFadeBackground
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val StageShape = RoundedCornerShape(48.dp)
private val ChipShape = RoundedCornerShape(21.7.dp)

/** "BPM" 글자와 속도 칩 글자. 공용 타이포 토큰에 없는 크기라 여기서만 쓴다. */
private val BpmUnitStyle = AppTypography.TitleLarge.copy(fontSize = 49.95.sp, lineHeight = 61.05.sp, fontWeight = FontWeight.Bold)
private val SpeedChipStyle = AppTypography.TitleLarge.copy(fontSize = 51.39.sp, lineHeight = 66.07.sp, fontWeight = FontWeight.Bold)

/** 시안의 박자 막대 높이. 앞 10개 뒤로 6개 패턴이 반복된다. */
private val BeatBarHeights = listOf(26.18f, 52.36f, 78.55f, 43.64f, 96f, 65.45f, 34.91f, 82.91f, 56.73f, 30.55f) +
    List(5) { listOf(69.82f, 91.64f, 48f, 32.73f, 61.09f, 21.82f) }.flatten()
private val BeatBarWidth = 13.09.dp
private val BeatBarGap = 10.91.dp
private const val MIN_BAR_SCALE = 0.6f
private const val BAR_PHASE_STEP = 0.07f

/** S03 차량 베이스 비트. 속도로 BPM을 정하고, 오늘 함께하는 가족을 보여 준다. */
@Composable
fun BaseBeatScreen(
    speedKmh: Float,
    family: List<FamilyMember>,
    onBack: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onStartRecording: () -> Unit,
) {
    val bpm = bpmForSpeed(speedKmh)
    SubPage(title = "차가 비트를 만들어요", onBack = onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.weight(1f)) {
            BeatStage(
                bpm = bpm,
                speedLabel = "속도 ${speedKmh.toInt()} km/h",
                bandLabel = "${speedBandLabel(speedKmh)} · ${beatTypeForSpeed(speedKmh)}비트",
                modifier = Modifier.weight(1f),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier.padding(start = 26.dp, top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(29.dp),
                ) {
                    Text("오늘 함께하는 가족", style = AppTypography.HeadlineLarge, color = AppColors.TextDarkPrimary)
                    Text("각자 어울리는 악기로 하나의 비트를 만들어요", style = AppTypography.TitleMedium, color = AppColors.TextWarm)
                }
                Column(
                    modifier = Modifier.padding(start = 19.dp, top = 72.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        family.forEach { member ->
                            InstrumentOption(
                                instrument = member.instrument,
                                name = member.name,
                                season = member.instrument.label,
                                modifier = Modifier.size(width = 235.dp, height = 392.dp),
                            )
                        }
                    }
                    SettingRow(
                        badge = SettingBadge.Voice,
                        title = "목소리 설정",
                        subtitle = "가족의 목소리를 녹음에 추가해요",
                        onClick = onOpenVoiceSettings,
                        modifier = Modifier.width(735.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                LargeButton(
                    text = "녹음 시작하기",
                    onClick = onStartRecording,
                    icon = R.drawable.ic_mic,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun BeatStage(bpm: Int, speedLabel: String, bandLabel: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(StageShape)
            .background(StageFadeBackground),
    ) {
        Row(
            modifier = Modifier
                .offset(35.dp, 35.dp)
                .clip(CircleShape)
                .background(AppColors.SurfaceBasic)
                .padding(start = 20.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_music), contentDescription = null, tint = AppColors.BrandOrange, modifier = Modifier.size(36.dp))
            Text("비트에 맞춰 준비 중", style = AppTypography.BodySmallStrong, color = AppColors.TextDarkSecondary)
        }
        Column(modifier = Modifier.offset(55.dp, 169.dp)) {
            Text("지금 차가 만드는 비트", style = AppTypography.TitleSmallNormal, color = AppColors.TextDarkTertiary)
            Row(horizontalArrangement = Arrangement.spacedBy(22.2.dp)) {
                Text(bpm.toString(), style = AppTypography.BeatVolume, color = AppColors.TextDarkPrimary, modifier = Modifier.alignByBaseline())
                Text("BPM", style = BpmUnitStyle, color = AppColors.TextDarkTertiary, modifier = Modifier.alignByBaseline())
            }
        }
        BeatBars(
            bpm = bpm,
            modifier = Modifier
                .offset(57.dp, 459.dp)
                .size(width = 660.dp, height = 96.dp),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 800.dp),
            horizontalArrangement = Arrangement.spacedBy(21.9.dp),
        ) {
            listOf(speedLabel, bandLabel).forEach { label ->
                Box(
                    modifier = Modifier
                        .height(86.7.dp)
                        .clip(ChipShape)
                        .background(AppColors.SurfaceBasic)
                        .padding(horizontal = 32.5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = SpeedChipStyle, color = AppColors.TextDarkSecondary)
                }
            }
        }
    }
}

/** BPM 박자마다 막대가 튀어 올랐다가 가라앉는다. 막대마다 박자를 조금씩 늦춰 물결처럼 보이게 한다. */
@Composable
private fun BeatBars(bpm: Int, modifier: Modifier = Modifier) {
    val beatMillis = 60_000 / bpm
    val phase by rememberInfiniteTransition(label = "beat").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(beatMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "beatPhase",
    )
    Canvas(modifier = modifier.clipToBounds()) {
        val barWidth = BeatBarWidth.toPx()
        val pitch = barWidth + BeatBarGap.toPx()
        val radius = CornerRadius(6.55.dp.toPx())
        val centerY = size.height / 2
        BeatBarHeights.forEachIndexed { index, heightDp ->
            val x = index * pitch
            if (x > size.width) return@forEachIndexed
            val t = (phase + index * BAR_PHASE_STEP) % 1f
            val pulse = (1f - t) * (1f - t)
            val h = heightDp.dp.toPx() * (MIN_BAR_SCALE + (1f - MIN_BAR_SCALE) * pulse)
            drawRoundRect(
                color = AppColors.BrandOrange,
                topLeft = Offset(x, centerY - h / 2),
                size = Size(barWidth, h),
                cornerRadius = radius,
            )
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun BaseBeatScreenPreview() {
    BaseBeatScreen(speedKmh = 42f, family = FakeFamily.members, onBack = {}, onOpenVoiceSettings = {}, onStartRecording = {})
}
