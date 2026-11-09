package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.family.FakeFamily
import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.family.MAX_FAMILY_MEMBERS
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentBadge
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentPictogram
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val SlotShape = RoundedCornerShape(32.dp)
private val SlotRadius = 32.dp
private val PillShape = RoundedCornerShape(16.dp)
private val BORDER_WIDTH = 2.dp
private val DASH_WIDTH = 3.dp

/** 안내 제목 (48/64 ExtraBold). 공용 타이포 토큰에 없는 크기라 여기서만 쓴다. */
private val LeadTitleStyle = AppTypography.HeadlineLarge.copy(fontSize = 48.sp, lineHeight = 64.sp)
private val PillTextStyle = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Bold)

/** S02-1 목소리 설정. 가족을 최대 4명까지 2×2 칸으로 보여 주고, 빈 칸에서 새 가족을 추가한다. */
@Composable
fun VoiceSettingsScreen(
    members: List<FamilyMember>,
    onBack: () -> Unit,
    onEdit: (FamilyMember) -> Unit,
    onAdd: () -> Unit,
    onDone: () -> Unit,
    onLater: () -> Unit,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    SubPage(title = "목소리 설정", onBack = onBack, overlay = overlay) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("목소리 등록 후", style = LeadTitleStyle, color = AppColors.TextDarkPrimary)
            Text(
                "가족을 추가할 때 이름을 입력하고 목소리를 녹음한 뒤, 사용할 악기를 선택해요.\n" +
                    "등록한 가족 정보는 언제든지 ‘수정’에서 이름, 목소리, 악기를 변경할 수 있어요.",
                style = AppTypography.BodyMedium,
                color = AppColors.TextWarmSecondary,
            )
        }
        Column(
            modifier = Modifier.padding(top = 44.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 등록된 가족 뒤로 빈 칸을 채워 항상 4칸을 2줄로 보여 준다
            val slots: List<FamilyMember?> = members.take(MAX_FAMILY_MEMBERS) + List((MAX_FAMILY_MEMBERS - members.size).coerceAtLeast(0)) { null }
            slots.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { member ->
                        if (member != null) {
                            MemberSlot(member, onEdit = { onEdit(member) })
                        } else {
                            EmptySlot(onAdd = onAdd)
                        }
                    }
                }
            }
        }
        Text(
            "나중에 해도 괜찮아요. 등록 전에는 노래가 완성될 때 목소리를 나눠 악기를 붙여요",
            style = AppTypography.LabelMedium,
            color = AppColors.TextDarkTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        )
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            LargeButton(text = "완료하기", onClick = onDone, icon = R.drawable.ic_check, modifier = Modifier.weight(1f))
            LargeButton(
                text = "나중에 하기",
                onClick = onLater,
                icon = R.drawable.ic_close,
                variant = LargeButtonVariant.Secondary,
                modifier = Modifier.width(599.dp),
            )
        }
    }
}

@Composable
private fun RowScope.MemberSlot(member: FamilyMember, onEdit: () -> Unit) {
    SlotFrame(
        borderWidth = BORDER_WIDTH,
        modifier = Modifier
            .clip(SlotShape)
            .background(AppColors.SurfaceBasic)
            .border(BORDER_WIDTH, AppColors.BasicAlphaLight50, SlotShape),
    ) {
        InstrumentBadge(member.instrument)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(member.name, style = AppTypography.TitleLargeExtraStrong, color = AppColors.TextDarkPrimary, maxLines = 1)
            Row(
                modifier = Modifier
                    .clip(PillShape)
                    .background(AppColors.BasicAlphaLight50)
                    .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InstrumentPictogram(member.instrument, size = 22.dp)
                Text(member.instrument.label, style = PillTextStyle, color = AppColors.TextDarkPrimary)
            }
            Text(if (member.hasVoice) "목소리 등록됨" else "목소리 등록 전", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
        }
        LargeButton(
            text = "수정",
            onClick = onEdit,
            icon = R.drawable.ic_edit,
            variant = LargeButtonVariant.Secondary,
            modifier = Modifier.width(248.dp),
        )
    }
}

@Composable
private fun RowScope.EmptySlot(onAdd: () -> Unit) {
    SlotFrame(borderWidth = DASH_WIDTH, modifier = Modifier.dashedBorder()) {
        Box(
            Modifier
                .size(120.dp)
                .background(AppColors.BasicGrayscale100, CircleShape),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("멤버 추가", style = AppTypography.TitleLargeExtraStrong, color = AppColors.TextDarkPrimary)
            Text("이름 · 목소리 · 악기", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
        }
        // 흰 바탕에 테두리가 있는 버튼이라 LargeButton과 모양이 달라 여기서 그린다
        Row(
            modifier = Modifier
                .width(248.dp)
                .height(100.dp)
                .clip(SlotShape)
                .background(AppColors.BackgroundSecondary)
                .border(BORDER_WIDTH, AppColors.ButtonBasicDisabled, SlotShape)
                .clickable(role = Role.Button, onClick = onAdd),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_mic), contentDescription = null, tint = AppColors.IconDarkPrimary, modifier = Modifier.size(52.dp))
            Text("추가", style = AppTypography.TitleSmall, color = AppColors.TextDarkPrimary)
        }
    }
}

/** 칸 공통: 높이 244, 안쪽 여백 30, 배지 · 글자 · 버튼을 가로로 놓는다. */
@Composable
private fun RowScope.SlotFrame(borderWidth: Dp, modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .weight(1f)
            .height(244.dp)
            .then(modifier)
            // 테두리는 공간을 차지하지 않으므로 여백에 두께를 더한다
            .padding(30.dp + borderWidth),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** 점선 테두리. Compose border는 점선을 지원하지 않아 직접 그린다. */
private fun Modifier.dashedBorder(): Modifier = drawBehind {
    val stroke = DASH_WIDTH.toPx()
    val inset = stroke / 2
    drawRoundRect(
        color = AppColors.BasicAlphaLight200,
        topLeft = Offset(inset, inset),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(SlotRadius.toPx() - inset),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4 * stroke, 3 * stroke))),
    )
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun VoiceSettingsScreenPreview() {
    VoiceSettingsScreen(
        members = FakeFamily.members.take(2),
        onBack = {},
        onEdit = {},
        onAdd = {},
        onDone = {},
        onLater = {},
    )
}
