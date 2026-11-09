package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.component.AppPage
import com.hmgappchallenge.kidsrhythm.ui.component.AppTab
import com.hmgappchallenge.kidsrhythm.ui.component.HintChip
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.Mascot
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.MascotPose
import com.hmgappchallenge.kidsrhythm.ui.component.SongRow
import com.hmgappchallenge.kidsrhythm.ui.component.SpeechBubble
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

/** 홈에 보여 줄 최근 곡 한 줄. */
data class RecentSong(val title: String, val meta: String)

/** 스테이지 바닥: 이 높이에서 시작해 아래로 옅어진다. */
private val StageFloorTop = 701.dp
private val StageFloorFadeHeight = 185.dp

@Composable
fun HomeScreen(
    recentSong: RecentSong?,
    isConsentVisible: Boolean,
    onStart: () -> Unit,
    onOpenArchive: () -> Unit,
    onPlayRecent: () -> Unit,
    onAgreeConsent: () -> Unit,
    onDismissConsent: () -> Unit,
) {
    AppPage(
        selectedTab = AppTab.Make,
        onSelectTab = { if (it == AppTab.Archive) onOpenArchive() },
        stageBackground = SolidColor(AppColors.StageBackground),
        stage = {
            StageFloor()
            SpeechBubble(
                text = "안녕! 우리 함께 노래를 만들어볼까?",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp),
            )
            Mascot(
                character = MascotCharacter.LION,
                pose = MascotPose.Sitting,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 304.dp)
                    // 시안에서 사자는 스테이지 가운데보다 28 왼쪽에 있다
                    .offset(x = (-28).dp)
                    .width(516.dp),
            )
            HintChip(
                text = "노래를 완성하면 친구가 나와서 춤춰요",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp),
            )
        },
        panel = {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 22.dp),
            ) {
                Column(
                    modifier = Modifier.padding(start = 26.dp),
                    verticalArrangement = Arrangement.spacedBy(29.dp),
                ) {
                    Text(text = "차가 달리는 리듬으로\n함께 노래를 만들어 보아요", style = AppTypography.HeadlineLarge, color = AppColors.TextDarkPrimary)
                    Text(text = "속도가 비트가 되고, 아이 목소리가 가사가 돼요.", style = AppTypography.TitleMedium, color = AppColors.TextWarmSecondary)
                }
                if (recentSong != null) {
                    Text(
                        text = "최근에 만든 노래",
                        style = AppTypography.LabelMedium,
                        color = AppColors.TextDarkTertiary,
                        modifier = Modifier.padding(start = 40.dp, top = 46.dp, bottom = 17.dp),
                    )
                    SongRow(title = recentSong.title, meta = recentSong.meta, onPlay = onPlayRecent, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.weight(1f))
            }
            LargeButton(
                text = "노래 만들기 시작",
                onClick = onStart,
                icon = R.drawable.ic_play,
                textStyle = AppTypography.TitleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        overlay = {
            if (isConsentVisible) {
                ConsentDialog(onAgree = onAgreeConsent, onDismiss = onDismissConsent)
            }
        },
    )
}

@Composable
private fun StageFloor() {
    Canvas(Modifier.fillMaxSize()) {
        val top = StageFloorTop.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(AppColors.StageFloor, Color.Transparent),
                startY = top,
                endY = top + StageFloorFadeHeight.toPx(),
            ),
            topLeft = Offset(0f, top),
            size = Size(size.width, size.height - top),
        )
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        recentSong = RecentSong("사과 노래", "어제 · 1분 42초"),
        isConsentVisible = false,
        onStart = {},
        onOpenArchive = {},
        onPlayRecent = {},
        onAgreeConsent = {},
        onDismissConsent = {},
    )
}
