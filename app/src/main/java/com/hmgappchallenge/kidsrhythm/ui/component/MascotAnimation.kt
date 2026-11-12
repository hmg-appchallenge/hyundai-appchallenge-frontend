package com.hmgappchallenge.kidsrhythm.ui.component

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.widget.ImageView
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.beat.SpeedZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 앉은 친구 애니메이션 (512px 움직이는 WebP, 24fps, 반복).
 * 생동감 있는 춤은 빠른 비트(8·16비트), 귀를 까딱이는 춤은 느린 비트(2·4비트)에 쓴다.
 */
enum class MascotAnimation(@RawRes val rawRes: Int) {
    Lively(R.raw.anim_lion_lively),
    Ears(R.raw.anim_lion_ears),
}

/** 이 친구·비트에 맞는 애니메이션. 아직 사자(부릉이)만 있어서 다른 친구는 null(멈춘 이미지)이다. */
fun mascotAnimationFor(character: MascotCharacter, zone: SpeedZone): MascotAnimation? {
    // TODO(디자인): 붕붕이·삐뽀·두두·빵빵이 애니메이션이 오면 더한다
    if (character != MascotCharacter.LION) return null
    return if (zone.beats >= 8) MascotAnimation.Lively else MascotAnimation.Ears
}

/** 움직이는 WebP를 반복 재생한다. 화면을 벗어나면 멈춘다. */
@Composable
fun AnimatedMascot(animation: MascotAnimation, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var drawable by remember(animation) { mutableStateOf<AnimatedImageDrawable?>(null) }
    LaunchedEffect(animation) {
        drawable = withContext(Dispatchers.IO) {
            ImageDecoder.decodeDrawable(ImageDecoder.createSource(context.resources, animation.rawRes)) as? AnimatedImageDrawable
        }?.apply {
            repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
            start()
        }
    }
    DisposableEffect(drawable) {
        // 정리할 때 지금 값을 다시 읽으면 새로 받은 애니메이션을 멈추게 되어, 이 효과가 시작될 때의 값을 잡아 둔다
        val playing = drawable
        onDispose { playing?.stop() }
    }
    AndroidView(
        factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
        update = { it.setImageDrawable(drawable) },
        modifier = modifier,
    )
}
