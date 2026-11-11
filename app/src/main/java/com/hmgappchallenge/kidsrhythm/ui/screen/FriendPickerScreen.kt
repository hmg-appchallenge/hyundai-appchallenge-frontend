package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.component.DancingMascot
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.Mascot
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.MascotPose
import com.hmgappchallenge.kidsrhythm.ui.component.SpeechBubble
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.component.friendName
import com.hmgappchallenge.kidsrhythm.ui.component.josa
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val CardShape = RoundedCornerShape(32.dp)
private val StageShape = RoundedCornerShape(48.dp)

/** 같이 놀 친구 고르기 (피그마 시안). 노래를 완성하면 고른 친구가 나와서 춤춘다. */
@Composable
fun FriendPickerScreen(current: MascotCharacter, onDecide: (MascotCharacter) -> Unit, onCancel: () -> Unit) {
    var selected by remember { mutableStateOf(current) }
    var clock by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) withFrameMillis { clock = it - start }
    }
    SubPage(title = "같이 놀 친구 고르기", onBack = onCancel) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text("노래를 완성하면 고른 친구가 나와서 춤춰요", style = AppTypography.TitleSmallNormal, color = AppColors.TextDarkSecondary)
                Column(modifier = Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    MascotCharacter.entries.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            row.forEach { c -> FriendCard(c, c == selected, Modifier.weight(1f)) { selected = c } }
                            if (row.size < 3) LockedCard(Modifier.weight(1f))
                            if (row.size < 2) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    LargeButton(text = "취소", onClick = onCancel, icon = R.drawable.ic_close, variant = LargeButtonVariant.Secondary, modifier = Modifier.width(300.dp))
                    LargeButton(text = "결정했어요", onClick = { onDecide(selected) }, icon = R.drawable.ic_check, modifier = Modifier.weight(1f))
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(StageShape)
                    .background(AppColors.StageBackground),
            ) {
                SpeechBubble(
                    text = "안녕! 나는 ${selected.friendName.josa("이야", "야")}",
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp),
                )
                DancingMascot(
                    character = selected,
                    positionMs = clock,
                    bpm = 100,
                    firstBeatMs = 0,
                    isPlaying = true,
                    modifier = Modifier.align(Alignment.Center).padding(top = 120.dp).width(520.dp),
                )
            }
        }
    }
}

@Composable
private fun FriendCard(character: MascotCharacter, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(300.dp)
            .clip(CardShape)
            .background(AppColors.SurfaceBasic)
            .border(if (isSelected) 4.dp else 2.dp, if (isSelected) AppColors.MusicPrimary else AppColors.BasicAlphaLight100, CardShape)
            .clickable(role = Role.RadioButton, onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Mascot(character, MascotPose.Sitting, Modifier.size(170.dp))
            Text(character.friendName, style = AppTypography.TitleSmall, color = AppColors.TextDarkPrimary)
            Text(character.label, style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(44.dp)
                    .background(AppColors.MusicPrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = "선택됨", tint = AppColors.BasicStaticLight100, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun LockedCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(300.dp)
            .clip(CardShape)
            .background(AppColors.SurfaceBasic)
            .border(2.dp, AppColors.BasicAlphaLight100, CardShape),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.size(120.dp).background(AppColors.BasicAlphaLight50, CircleShape))
        Text("새 친구", style = AppTypography.TitleSmall, color = AppColors.TextDarkTertiary)
        Text("곧 만나요", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun FriendPickerPreview() {
    FriendPickerScreen(current = MascotCharacter.CAT, onDecide = {}, onCancel = {})
}
