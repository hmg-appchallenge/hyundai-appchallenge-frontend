package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.PopupOverlay
import com.hmgappchallenge.kidsrhythm.ui.component.SettingBadge
import com.hmgappchallenge.kidsrhythm.ui.component.SettingRow
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val ItemsShape = RoundedCornerShape(32.dp)
private val BORDER_WIDTH = 2.dp

private data class ConsentItem(@DrawableRes val icon: Int, val title: String, val description: String)

private val ConsentItems = listOf(
    ConsentItem(R.drawable.ic_mic, "무엇을 쓰나요", "멤버 목소리를 녹음해 가사와 노래를 만들어요"),
    ConsentItem(R.drawable.ic_shield, "어디로 보내나요", "음성 인식(Pleos Gleo AI)과 노래 생성 서버로 보내요. 학습에는 쓰지 않아요"),
    ConsentItem(R.drawable.ic_trash, "언제 지우나요", "녹음 원본은 노래가 완성되면 바로 지우고, 노래는 이 차에만 저장해요"),
)

/** S00 보호자 동의 팝업. 앱 창을 어둡게 덮고 가운데에 뜬다. */
@Composable
fun BoxScope.ConsentDialog(onAgree: () -> Unit, onDismiss: () -> Unit) {
    PopupOverlay(topPadding = 144.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
                Text("아이 목소리를 사용해도 될까요?", style = AppTypography.TitleLarge, color = AppColors.TextDarkPrimary)
                Text(
                    "노래를 만들려면 보호자의 동의가 필요해요.\n처음 한 번만 물어보고, 이어서 멤버 목소리를 등록해요.",
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextWarm,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ItemsShape)
                    .background(AppColors.BasicAlphaLight50)
                    .padding(horizontal = 32.dp, vertical = 8.dp),
            ) {
                ConsentItems.forEachIndexed { index, item ->
                    ConsentItemRow(item, showDivider = index < ConsentItems.lastIndex)
                }
            }
            // TODO(기획): 개인정보 처리방침 주소가 정해지면 연결한다
            SettingRow(
                badge = SettingBadge.Privacy,
                title = "개인정보 처리방침 자세히 보기",
                subtitle = "설정에서 언제든 철회할 수 있어요",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LargeButton(
                    text = "동의하고 시작",
                    onClick = onAgree,
                    icon = R.drawable.ic_check,
                    textStyle = AppTypography.TitleSmallExtraStrong,
                    modifier = Modifier.weight(1f),
                )
                LargeButton(
                    text = "나중에",
                    onClick = onDismiss,
                    icon = R.drawable.ic_close,
                    variant = LargeButtonVariant.Secondary,
                    textStyle = AppTypography.TitleSmallExtraStrong,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ConsentItemRow(item: ConsentItem, showDivider: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (showDivider) Modifier.bottomDivider() else Modifier)
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppColors.SurfaceBasic),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(item.icon), contentDescription = null, tint = AppColors.IconDarkPrimary, modifier = Modifier.size(40.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, style = AppTypography.BodyLargeStrong, color = AppColors.TextDarkPrimary)
            Text(item.description, style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
        }
    }
}

private fun Modifier.bottomDivider(): Modifier = drawBehind {
    val stroke = BORDER_WIDTH.toPx()
    val y = size.height - stroke / 2
    drawLine(AppColors.BasicAlphaLight100, Offset(0f, y), Offset(size.width, y), strokeWidth = stroke)
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun ConsentDialogPreview() {
    Box(Modifier.fillMaxSize()) { ConsentDialog(onAgree = {}, onDismiss = {}) }
}
