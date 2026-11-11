package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.family.introduceMessage
import com.hmgappchallenge.kidsrhythm.ui.component.HintChip
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.MascotPose
import com.hmgappchallenge.kidsrhythm.ui.component.SpeechBubble
import com.hmgappchallenge.kidsrhythm.ui.component.StageFloor
import com.hmgappchallenge.kidsrhythm.ui.component.StandingCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val StageShape = RoundedCornerShape(48.dp)
private val CardShape = RoundedCornerShape(32.dp)
private val CARD_BORDER = 2.dp
private val SELECTED_CARD_BORDER = 4.dp
private val ThumbSize = 176.dp

/** 카드 글자. 공용 타이포 토큰에 없는 굵기·크기라 여기서만 쓴다. */
private val CardNameStyle = AppTypography.BodyLargeStrong.copy(fontWeight = FontWeight.ExtraBold)
private val CardLabelStyle = AppTypography.LabelMedium.copy(fontSize = 22.sp, lineHeight = 28.sp)

/** 카드 동그라미 바탕색. 시안이 악기 배경색을 그대로 가져다 쓴다. */
private val MascotCharacter.thumbBackground: Color
    get() = when (this) {
        MascotCharacter.DOG -> AppColors.MaracasBackground
        MascotCharacter.CAT -> AppColors.BellBackground
        MascotCharacter.FROG -> AppColors.GuitarBackground
        MascotCharacter.PIG -> AppColors.DrumBackground
        MascotCharacter.LION -> AppColors.TambourineBackground
    }

/**
 * S01-1 같이 놀 친구 고르기. 카드를 누르면 왼쪽 스테이지에 미리 보여 주고,
 * [결정했어요]를 눌러야 [onConfirm]으로 저장한다. 뒤로 가면 고르기 전 친구 그대로다.
 */
@Composable
fun CharacterPickerScreen(
    current: MascotCharacter,
    onConfirm: (MascotCharacter) -> Unit,
    onBack: () -> Unit,
) {
    var draft by remember(current) { mutableStateOf(current) }
    SubPage(title = "같이 놀 친구 고르기", onBack = onBack) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(StageShape)
                    .background(AppColors.StageBackground),
            ) {
                StageFloor()
                SpeechBubble(
                    text = introduceMessage(draft.nickname),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 120.dp),
                )
                StandingCharacter(draft)
                HintChip(
                    text = "함께 놀 친구를 미리 골라보아요",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 56.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Text(
                    text = "노래를 완성하면 고른 친구가 나와서 춤춰요",
                    style = AppTypography.TitleMedium,
                    color = AppColors.TextWarm,
                    modifier = Modifier.padding(start = 26.dp, top = 22.dp),
                )
                CharacterGrid(
                    selected = draft,
                    onSelect = { draft = it },
                    modifier = Modifier.padding(top = 23.dp),
                )
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    LargeButton(
                        text = "결정했어요",
                        onClick = { onConfirm(draft) },
                        icon = R.drawable.ic_check,
                        modifier = Modifier.weight(1f),
                    )
                    LargeButton(
                        text = "뒤로 가기",
                        onClick = onBack,
                        icon = R.drawable.ic_replay,
                        variant = LargeButtonVariant.Secondary,
                        modifier = Modifier.width(266.dp),
                    )
                }
            }
        }
    }
}

/** 친구 5명과 잠긴 "새 친구" 칸을 3×2로 놓는다. */
@Composable
private fun CharacterGrid(selected: MascotCharacter, onSelect: (MascotCharacter) -> Unit, modifier: Modifier = Modifier) {
    val cells: List<MascotCharacter?> = MascotCharacter.entries + null
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        cells.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { character ->
                    if (character == null) {
                        LockedCard()
                    } else {
                        CharacterCard(character, isSelected = character == selected, onClick = { onSelect(character) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterCard(character: MascotCharacter, isSelected: Boolean, onClick: () -> Unit) {
    val border = if (isSelected) SELECTED_CARD_BORDER else CARD_BORDER
    Box(
        modifier = Modifier
            .size(width = 242.dp, height = 320.dp)
            .clip(CardShape)
            .background(AppColors.PopupWarmBackground)
            .border(border, if (isSelected) AppColors.BrandOrange else AppColors.PopupWarmBorder, CardShape)
            .clickable(role = Role.RadioButton, onClick = onClick),
    ) {
        CardContent(
            name = character.nickname,
            label = character.label,
            nameColor = AppColors.TextDarkPrimary,
            labelColor = AppColors.TextWarm,
        ) {
            Box(
                modifier = Modifier
                    .size(ThumbSize)
                    .clip(CircleShape)
                    .background(character.thumbBackground),
            ) {
                // 앉은 친구가 동그라미 아래에 걸쳐 앉은 모습
                Image(
                    painter = painterResource(character.imageRes(MascotPose.Sitting)),
                    contentDescription = character.nickname,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 8.dp)
                        .height(170.dp),
                )
            }
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .offset(x = 174.dp, y = 12.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.BrandOrange),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = "선택됨", tint = AppColors.PopupWarmBackground, modifier = Modifier.size(32.dp))
            }
        }
    }
}

/** 아직 만날 수 없는 친구 칸. 누를 수 없다. */
@Composable
private fun LockedCard() {
    Box(
        modifier = Modifier
            .size(width = 242.dp, height = 320.dp)
            .clip(CardShape)
            .background(AppColors.PopupWarmBackground)
            .border(CARD_BORDER, AppColors.PopupWarmBorder, CardShape),
    ) {
        CardContent(
            name = "새 친구",
            label = "곧 만나요!",
            nameColor = AppColors.CharacterLockedName,
            labelColor = AppColors.CharacterLockedLabel,
        ) {
            Box(
                modifier = Modifier
                    .size(ThumbSize)
                    .clip(CircleShape)
                    .background(AppColors.ButtonWarmSecondary),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(R.drawable.ic_lock), contentDescription = null, modifier = Modifier.size(56.dp))
            }
        }
    }
}

@Composable
private fun CardContent(
    name: String,
    label: String,
    nameColor: Color,
    labelColor: Color,
    thumb: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        thumb()
        Text(name, style = CardNameStyle, color = nameColor, maxLines = 1)
        Text(label, style = CardLabelStyle, color = labelColor, maxLines = 1)
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun CharacterPickerScreenPreview() {
    CharacterPickerScreen(current = MascotCharacter.LION, onConfirm = {}, onBack = {})
}
