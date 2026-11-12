package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

/** 버튼이 한두 개인 안내 팝업 (멤버 삭제 팝업과 같은 모양). [secondaryText]가 없으면 버튼 하나. */
@Composable
fun BoxScope.MessagePopup(
    title: String,
    message: String,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: () -> Unit = {},
    @DrawableRes primaryIcon: Int? = null,
    @DrawableRes secondaryIcon: Int? = null,
    destructive: Boolean = false,
) {
    PopupOverlay(topPadding = 260.dp) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(title, style = AppTypography.TitleLarge, color = AppColors.TextDarkPrimary)
            Text(message, style = AppTypography.BodyMedium, color = AppColors.TextDarkSecondary)
            Row(modifier = Modifier.padding(top = 28.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (secondaryText != null) {
                    LargeButton(
                        text = secondaryText,
                        onClick = onSecondary,
                        icon = secondaryIcon,
                        variant = LargeButtonVariant.Secondary,
                        modifier = Modifier.weight(1f),
                    )
                }
                LargeButton(
                    text = primaryText,
                    onClick = onPrimary,
                    icon = primaryIcon,
                    variant = if (destructive) LargeButtonVariant.Destructive else LargeButtonVariant.Primary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
