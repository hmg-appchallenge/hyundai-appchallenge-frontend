package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import kotlinx.coroutines.delay

/** 시작 화면을 보여 주는 시간. 누르면 바로 넘어간다. */
private const val SPLASH_MILLIS = 2_000L

private val SplashShape = RoundedCornerShape(40.dp)

/** 앱 창의 가로:세로가 이보다 넓으면(왼쪽 차량 화면 없이 2560 전체를 쓸 때) 넓은 그림을 쓴다. 기본 창은 1.42, 전체 화면은 2.16. */
private const val WIDE_ASPECT = 1.8f

/**
 * 앱을 켰을 때 잠깐 보이는 시작 화면. 로고, 소개 문구, 가족이 탄 자동차 그림을 한 장의 그림으로 보여 준다.
 * 움직이거나 바뀌는 곳이 없어 시안을 그대로 쓴다.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(SPLASH_MILLIS)
        finish()
    }
    BoxWithConstraints {
    val wide = maxWidth / maxHeight > WIDE_ASPECT
    Image(
        painter = painterResource(if (wide) R.drawable.img_splash_wide else R.drawable.img_splash),
        contentDescription = "차곡차곡. 차 안에서 가족의 목소리를 모아, 세상에 하나뿐인 우리 가족만의 동요를 만드는 패밀리 뮤직 서비스",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .clip(SplashShape)
            .background(AppColors.SplashBackground)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { finish() },
    )
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun SplashScreenPreview() {
    SplashScreen(onFinished = {})
}
