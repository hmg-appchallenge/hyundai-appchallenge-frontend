package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

/** 따뜻한 흰 바탕의 확인 팝업 (너비 808). 왼쪽은 취소 쪽 회색 버튼, 오른쪽은 [confirmVariant] 버튼이다. */
@Composable
fun BoxScope.WarmConfirmPopup(
    title: String,
    message: String,
    cancelText: String,
    onCancel: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    @DrawableRes confirmIcon: Int,
    confirmVariant: LargeButtonVariant = LargeButtonVariant.Destructive,
    @DrawableRes cancelIcon: Int = R.drawable.ic_close,
) {
    PopupOverlay(
        topPadding = 402.dp,
        width = 808.dp,
        background = AppColors.PopupWarmBackground,
        borderColor = AppColors.PopupWarmBorder,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = AppTypography.TitleLarge, color = AppColors.TextDarkPrimary)
                Text(message, style = AppTypography.BodyLargeStrong, color = AppColors.SongMeta)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LargeButton(
                    text = cancelText,
                    onClick = onCancel,
                    icon = cancelIcon,
                    variant = LargeButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                LargeButton(text = confirmText, onClick = onConfirm, icon = confirmIcon, variant = confirmVariant, modifier = Modifier.weight(1f))
            }
        }
    }
}
