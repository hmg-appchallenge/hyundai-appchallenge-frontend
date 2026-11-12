package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.component.AppPage
import com.hmgappchallenge.kidsrhythm.ui.component.AppTab
import com.hmgappchallenge.kidsrhythm.ui.component.HintChip
import com.hmgappchallenge.kidsrhythm.ui.component.IconSquareButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.Mascot
import com.hmgappchallenge.kidsrhythm.ui.component.NoticeSnackbar
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.SongRow
import com.hmgappchallenge.kidsrhythm.ui.component.SpeechBubble
import com.hmgappchallenge.kidsrhythm.ui.component.StageFloor
import com.hmgappchallenge.kidsrhythm.ui.component.StandingCharacter
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlinx.coroutines.delay

private const val CONSENT_NOTICE_MILLIS = 3_000L

/** 홈에 보여 줄 최근 곡 한 줄. */
data class RecentSong(val title: String, val meta: String)

/** 스테이지 바닥: 이 높이에서 시작해 아래로 옅어진다. */

@Composable
fun HomeScreen(
    character: MascotCharacter,
    recentSong: RecentSong?,
    isConsentVisible: Boolean,
    onStart: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onOpenCharacterPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenArchive: () -> Unit,
    onPlayRecent: () -> Unit,
    onAgreeConsent: () -> Unit,
    onDismissConsent: () -> Unit,
    onOpenPrivacy: () -> Unit = {},
    consentNoticeCount: Int = 0,
    onTapConsentNotice: () -> Unit = {},
) {
    // 동의 없이 시작을 누를 때마다 안내를 새로 띄우고 잠시 뒤 숨긴다
    // 홈에 다시 들어왔을 때 지난 안내를 또 띄우지 않도록, 띄운 횟수를 기억한다
    var isNoticeVisible by remember { mutableStateOf(false) }
    var shownNoticeCount by rememberSaveable { mutableIntStateOf(consentNoticeCount) }
    LaunchedEffect(consentNoticeCount) {
        if (consentNoticeCount == shownNoticeCount) return@LaunchedEffect
        shownNoticeCount = consentNoticeCount
        isNoticeVisible = true
        delay(CONSENT_NOTICE_MILLIS)
        isNoticeVisible = false
    }
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
            StandingCharacter(character)
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
        headerActions = {
            LargeButton(
                text = "목소리 설정",
                onClick = onOpenVoiceSettings,
                icon = R.drawable.ic_mic,
                variant = LargeButtonVariant.Secondary,
                modifier = Modifier.widthIn(min = 320.dp),
            )
            LargeButton(
                text = "친구 고르기",
                onClick = onOpenCharacterPicker,
                icon = R.drawable.ic_paw,
                variant = LargeButtonVariant.Secondary,
                // 글자가 잘리지 않도록 시안 너비를 최소값으로 둔다
                modifier = Modifier.widthIn(min = 279.dp),
            )
            IconSquareButton(icon = R.drawable.ic_settings, contentDescription = "설정", onClick = onOpenSettings)
        },
        overlay = {
            if (isConsentVisible) {
                ConsentDialog(onAgree = onAgreeConsent, onDismiss = onDismissConsent, onOpenPrivacy = onOpenPrivacy)
            } else if (isNoticeVisible) {
                NoticeSnackbar(
                    icon = R.drawable.ic_shield,
                    title = "보호자 동의가 있어야 노래를 만들 수 있어요",
                    description = "아이 목소리를 녹음하기 때문에 동의가 필요해요",
                    onClick = {
                        isNoticeVisible = false
                        onTapConsentNotice()
                    },
                )
            }
        },
    )
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        character = MascotCharacter.LION,
        recentSong = RecentSong("사과 노래", "어제 · 1분 42초"),
        isConsentVisible = false,
        onStart = {},
        onOpenVoiceSettings = {},
        onOpenCharacterPicker = {},
        onOpenSettings = {},
        onOpenArchive = {},
        onPlayRecent = {},
        onAgreeConsent = {},
        onDismissConsent = {},
    )
}
