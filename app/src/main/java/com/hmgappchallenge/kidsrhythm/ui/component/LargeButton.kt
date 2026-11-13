package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

/**
 * Primary는 주황, Secondary는 따뜻한 회색, Destructive는 삭제용 빨강, Dark는 진한 갈색, Filled는 진한 회색 바탕이다.
 * Outlined는 흰 바탕에 옅은 테두리와 주황 글자다.
 */
enum class LargeButtonVariant { Primary, Secondary, Destructive, Dark, Filled, Outlined }

private val ButtonShape = RoundedCornerShape(32.dp)
private const val DISABLED_ALPHA = 0.4f

/** 높이 100의 큰 버튼. */
@Composable
fun LargeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    variant: LargeButtonVariant = LargeButtonVariant.Primary,
    textStyle: TextStyle = AppTypography.TitleSmall,
) {
    val contentColor = when (variant) {
        LargeButtonVariant.Primary -> AppColors.PopupWarmBackground
        LargeButtonVariant.Secondary -> AppColors.TextWarmStrong
        LargeButtonVariant.Destructive -> AppColors.PopupWarmBackground
        LargeButtonVariant.Dark -> AppColors.PopupWarmBackground
        LargeButtonVariant.Filled -> AppColors.BasicStaticLight100
        LargeButtonVariant.Outlined -> AppColors.BrandOrange
    }
    val background = when (variant) {
        LargeButtonVariant.Primary -> AppColors.BrandOrange
        LargeButtonVariant.Secondary -> AppColors.ButtonWarmSecondary
        LargeButtonVariant.Destructive -> AppColors.InformativeNegative
        LargeButtonVariant.Dark -> AppColors.ReplayHint
        LargeButtonVariant.Filled -> AppColors.ButtonFilledEnabled
        LargeButtonVariant.Outlined -> AppColors.PopupWarmBackground
    }
    Row(
        modifier = modifier
            .height(100.dp)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(ButtonShape)
            .background(background)
            .then(if (variant == LargeButtonVariant.Outlined) Modifier.border(2.dp, AppColors.PopupWarmBorder, ButtonShape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 40.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = contentColor, modifier = Modifier.size(52.dp))
        }
        Text(text = text, style = textStyle, color = contentColor, maxLines = 1)
    }
}

/** 아이콘만 있는 100×100 회색 버튼 (홈 설정 버튼 등). */
@Composable
fun IconSquareButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(ButtonShape)
            .background(AppColors.ButtonWarmSecondary)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painterResource(icon), contentDescription = contentDescription, tint = AppColors.TextWarmStrong, modifier = Modifier.size(52.dp))
    }
}

@Preview
@Composable
private fun LargeButtonPreview() {
    LargeButton(text = "노래 만들기 시작", onClick = {}, icon = R.drawable.ic_play, textStyle = AppTypography.TitleMedium)
}
