package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R

private const val SPINNER_TURN_MILLIS = 1_000

/** 고리 지름이 바깥 상자의 몇 배인지. 상자 가장자리에 여백을 두고 고리를 가운데 그린다. */
private const val RING_RATIO = 100f / 120f

/** 계속 도는 로딩 고리. [size]는 여백을 포함한 바깥 크기다. */
@Composable
fun LoadingSpinner(size: Dp, modifier: Modifier = Modifier, contentDescription: String? = "준비 중") {
    val rotation by rememberInfiniteTransition(label = "spinner").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(SPINNER_TURN_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "spinnerRotation",
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.img_loading_spinner),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * RING_RATIO),
        )
    }
}

@Preview
@Composable
private fun LoadingSpinnerPreview() {
    LoadingSpinner(size = 120.dp)
}
