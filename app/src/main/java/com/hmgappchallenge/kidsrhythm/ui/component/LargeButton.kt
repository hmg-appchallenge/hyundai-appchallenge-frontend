package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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

enum class LargeButtonVariant { Primary, Secondary }

private val ButtonShape = RoundedCornerShape(32.dp)
private const val DISABLED_ALPHA = 0.4f

/** 높이 100의 큰 버튼. Primary는 주황 바탕, Secondary는 옅은 회색 바탕이다. */
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
    val primary = variant == LargeButtonVariant.Primary
    val contentColor = if (primary) AppColors.BasicStaticLight100 else AppColors.TextDarkPrimary
    Row(
        modifier = modifier
            .height(100.dp)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(ButtonShape)
            .background(if (primary) AppColors.BrandOrange else AppColors.BasicAlphaLight50)
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

@Preview
@Composable
private fun LargeButtonPreview() {
    LargeButton(text = "노래 만들기 시작", onClick = {}, icon = R.drawable.ic_play, textStyle = AppTypography.TitleMedium)
}
