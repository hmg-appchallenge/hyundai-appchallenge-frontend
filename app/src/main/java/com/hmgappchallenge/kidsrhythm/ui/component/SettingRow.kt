package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val RowShape = RoundedCornerShape(23.3.dp)
private val BadgeShape = RoundedCornerShape(25.63.dp)
private val BORDER_WIDTH = 2.dp

/** 설정 행의 왼쪽 배지 아이콘. 글리프마다 상자 안 위치가 달라서 크기와 위치를 함께 둔다. */
enum class SettingBadge(@DrawableRes val icon: Int, val iconSize: Dp, val iconOffset: DpOffset) {
    Privacy(R.drawable.ic_shield, 46.5.dp, DpOffset(19.14.dp, 17.57.dp)),
    Voice(R.drawable.ic_mic, 52.dp, DpOffset(16.06.dp, 16.67.dp)),
}

/** 회색 배지 · 제목 · 설명 · 오른쪽 화살표로 된 높이 151의 행. 누르면 다른 화면으로 이동한다. */
@Composable
fun SettingRow(
    badge: SettingBadge,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(151.dp)
            .clip(RowShape)
            .background(AppColors.BasicStaticLight100)
            .border(BORDER_WIDTH, AppColors.BasicAlphaLight100, RowShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 37.dp + BORDER_WIDTH),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(26.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(83.89.dp)
                    .clip(BadgeShape)
                    .background(AppColors.BasicGrayscale100),
            ) {
                Icon(
                    painter = painterResource(badge.icon),
                    contentDescription = null,
                    tint = AppColors.FieldFocused,
                    modifier = Modifier
                        .padding(start = badge.iconOffset.x, top = badge.iconOffset.y)
                        .size(badge.iconSize),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = AppTypography.TitleSmall, color = AppColors.TextDarkPrimary)
                Text(subtitle, style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
            }
        }
        Image(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
    }
}

@Preview
@Composable
private fun SettingRowPreview() {
    SettingRow(SettingBadge.Voice, "목소리 설정", "가족의 목소리를 녹음에 추가해요", onClick = {})
}
