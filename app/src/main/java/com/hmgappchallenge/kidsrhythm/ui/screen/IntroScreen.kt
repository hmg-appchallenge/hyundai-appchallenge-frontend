package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.component.AppPage
import com.hmgappchallenge.kidsrhythm.ui.component.AppTab
import com.hmgappchallenge.kidsrhythm.ui.component.HintChip
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.LoadingSpinner
import com.hmgappchallenge.kidsrhythm.ui.component.StageFadeBackground
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlinx.coroutines.delay

internal const val INTRO_DURATION_MILLIS = 3_000L


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
                size = 186.dp,
                modifier = Modifier
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

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun IntroScreenPreview() {
    IntroScreen(onFinished = {}, onCancel = {}, onOpenArchive = {})
}
