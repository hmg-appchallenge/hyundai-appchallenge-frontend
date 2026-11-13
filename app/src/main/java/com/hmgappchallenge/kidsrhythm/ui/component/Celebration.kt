package com.hmgappchallenge.kidsrhythm.ui.component

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.widget.ImageView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.hmgappchallenge.kidsrhythm.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** 폭죽 그림 한 번의 길이 (559프레임 × 20ms). */
private const val FIREWORK_MILLIS = 11_180L

/** 끝나기 전 이만큼 동안 서서히 사라진다. */
private const val FADE_OUT_MILLIS = 1_500

/** 처음 보일 때 불투명도. */
private const val START_ALPHA = 0.5f

/**
 * 노래를 처음 완성했을 때 무대 위에 한 번 터지는 주황 폭죽.
 * 불투명도 50%로 시작해 끝날 무렵 0%로 사라지고, 다 끝나면 [onEnd]를 부른다.
 */
@Composable
fun FireworkCelebration(onEnd: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val end by rememberUpdatedState(onEnd)
    val alpha = remember { Animatable(START_ALPHA) }
    var drawable by remember { mutableStateOf<AnimatedImageDrawable?>(null) }
    LaunchedEffect(Unit) {
        drawable = withContext(Dispatchers.IO) {
            ImageDecoder.decodeDrawable(ImageDecoder.createSource(context.resources, R.raw.anim_firework)) as? AnimatedImageDrawable
        }?.apply {
            repeatCount = 0
            start()
        }
        delay(FIREWORK_MILLIS - FADE_OUT_MILLIS)
        alpha.animateTo(0f, tween(FADE_OUT_MILLIS, easing = LinearEasing))
        end()
    }
    DisposableEffect(drawable) {
        val playing = drawable
        onDispose { playing?.stop() }
    }
    AndroidView(
        factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
        update = { it.setImageDrawable(drawable) },
        modifier = modifier.graphicsLayer { this.alpha = alpha.value },
    )
}
