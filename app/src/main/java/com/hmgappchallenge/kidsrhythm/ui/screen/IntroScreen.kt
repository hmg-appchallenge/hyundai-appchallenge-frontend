package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.component.AppPage
import com.hmgappchallenge.kidsrhythm.ui.component.AppTab
import com.hmgappchallenge.kidsrhythm.ui.component.HintChip
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.StageFadeBackground
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlinx.coroutines.delay

internal const val INTRO_DURATION_MILLIS = 3_000L
private const val SPINNER_TURN_MILLIS = 1_000


/** S02 시작 안내. 3초 뒤 자동으로 [onFinished]를 부른다. */
@Composable
fun IntroScreen(
    onFinished: () -> Unit,
    onCancel: () -> Unit,
    onOpenArchive: () -> Unit,
) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(INTRO_DURATION_MILLIS)
        currentOnFinished()
    }
    AppPage(
        selectedTab = AppTab.Make,
        onSelectTab = { if (it == AppTab.Archive) onOpenArchive() },
        stageBackground = StageFadeBackground,
        stage = {
            LoadingSpinner(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 354.dp),
            )
            HintChip(
                text = "3초 뒤에 자동으로 시작해요",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp),
            )
        },
        panel = {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 26.dp, top = 22.dp),
                verticalArrangement = Arrangement.spacedBy(29.dp),
            ) {
                Text(text = "노래 시작을\n준비하고 있어요", style = AppTypography.HeadlineLarge, color = AppColors.TextDarkPrimary)
                Text(
                    text = "스피커로 “음악 한번 만들어볼까?”\n안내가 나가기까지 기다려 주세요",
                    style = AppTypography.TitleMedium,
                    color = AppColors.TextWarmSecondary,
                )
                Spacer(Modifier.weight(1f))
            }
            LargeButton(
                text = "그만두기",
                onClick = onCancel,
                icon = R.drawable.ic_close,
                variant = LargeButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun LoadingSpinner(modifier: Modifier = Modifier) {
    val rotation by rememberInfiniteTransition(label = "spinner").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(SPINNER_TURN_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "spinnerRotation",
    )
    Box(
        modifier = modifier
            .size(186.dp)
            .graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.Center,
    ) {
        Image(painter = painterResource(R.drawable.img_loading_spinner), contentDescription = "준비 중")
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun IntroScreenPreview() {
    IntroScreen(onFinished = {}, onCancel = {}, onOpenArchive = {})
}
