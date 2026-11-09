package com.hmgappchallenge.kidsrhythm.ui.component

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp

/** 색·블러를 지정한 그림자. Compose `shadow`는 둘을 따로 정할 수 없어 직접 그린다. */
internal fun Modifier.dropShadow(color: Color, offsetY: Dp, blur: Dp, cornerRadius: Dp): Modifier = drawWithCache {
    val paint = Paint().asFrameworkPaint().apply {
        this.color = color.toArgb()
        // 블러 반경 = 2σ, BlurMaskFilter 반경 ≈ (σ - 0.5) / 0.57735
        maskFilter = BlurMaskFilter(((blur.toPx() / 2f) - 0.5f).coerceAtLeast(0.1f) / 0.57735f, BlurMaskFilter.Blur.NORMAL)
    }
    val radius = cornerRadius.toPx()
    val dy = offsetY.toPx()
    onDrawBehind {
        drawIntoCanvas { it.nativeCanvas.drawRoundRect(0f, dy, size.width, size.height + dy, radius, radius, paint) }
    }
}
