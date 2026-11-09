package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors

private val PopupShape = RoundedCornerShape(48.dp)
private val BORDER_WIDTH = 2.dp

/** 앱 창을 어둡게 덮고 가운데 위쪽에 띄우는 시스템 팝업 (너비 1200, 모서리 48). */
@Composable
fun BoxScope.PopupOverlay(
    topPadding: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    // 뒤 화면을 누를 수 없게 딤이 터치를 막는다
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(AppColors.Dim)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {}),
    )
    Box(
        modifier = modifier
            .align(Alignment.TopCenter)
            .padding(top = topPadding)
            .width(1200.dp)
            .clip(PopupShape)
            .background(AppColors.BackgroundPopup)
            .border(BORDER_WIDTH, AppColors.BasicAlphaLight50, PopupShape)
            .padding(48.dp + BORDER_WIDTH),
        content = content,
    )
}
