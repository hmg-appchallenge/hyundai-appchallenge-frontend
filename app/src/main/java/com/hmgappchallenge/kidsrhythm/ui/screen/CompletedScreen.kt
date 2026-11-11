package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.AppServices
import com.hmgappchallenge.kidsrhythm.CompletedSong
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.archive.ArchivedMember
import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.ui.component.DancingMascot
import com.hmgappchallenge.kidsrhythm.ui.component.HintChip
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.LyricsBox
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerBar
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerTicker
import com.hmgappchallenge.kidsrhythm.ui.component.SpeechBubble
import com.hmgappchallenge.kidsrhythm.ui.component.StageFloor
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.component.friendName
import com.hmgappchallenge.kidsrhythm.ui.component.josa
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlinx.coroutines.launch

private val StageShape = RoundedCornerShape(48.dp)
private val StageWidth = 774.dp
private val ChipShape = RoundedCornerShape(24.dp)

/** S06 완성. 받아 둔 노래를 바로 틀고, 친구가 박자에 맞춰 춤춘다. 보관함에 저장하거나 한 곡 더 만든다. */
@Composable
fun CompletedRoute(
    services: AppServices,
    family: List<FamilyMember>,
    onSaved: () -> Unit,
    onMakeAnother: () -> Unit,
    onChangeFriend: () -> Unit,
    onBack: () -> Unit,
) {
    // 처음 들어올 때의 곡을 붙잡아 둔다. 저장하면서 completedSong을 비워도 이 화면이 '곡 없음'으로 홈에 돌아가지 않게
    val song = remember { services.completedSong }
    if (song == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val player = services.player
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    LaunchedEffect(song.songId) {
        services.beatPlayer.stop()
        player.load(song.audioFile.path, autoplay = true, knownDurationMs = song.result.durationMs)
    }
    DisposableEffect(Unit) { onDispose { player.pause() } }
    PlayerTicker(player)
    CompletedScreen(
        song = song,
        friend = services.friend,
        isPlaying = player.isPlaying,
        positionMs = player.positionMs,
        durationMs = player.durationMs.takeIf { it > 0 } ?: song.result.durationMs,
        saving = saving,
        onToggle = player::toggle,
        onSeek = player::seekTo,
        onSave = {
            if (!saving) {
                saving = true
                scope.launch {
                    val members = song.result.members.map { m ->
                        ArchivedMember(m.memberId, family.firstOrNull { it.id == m.memberId }?.name.orEmpty(), m.instrument)
                    }
                    services.archive.import(song.result, song.audioFile, members, song.createdAtMillis)
                    services.completedSong = null
                    onSaved()
                }
            }
        },
        onMakeAnother = onMakeAnother,
        onChangeFriend = onChangeFriend,
        onBack = onBack,
    )
}

@Composable
fun CompletedScreen(
    song: CompletedSong,
    friend: MascotCharacter,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    saving: Boolean,
    onToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onSave: () -> Unit,
    onMakeAnother: () -> Unit,
    onChangeFriend: () -> Unit,
    onBack: () -> Unit,
) {
    SubPage(title = "완성!", onBack = onBack) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(52.dp)) {
            Box(
                modifier = Modifier
                    .width(StageWidth)
                    .fillMaxHeight()
                    .clip(StageShape)
                    .background(AppColors.StageBackground),
            ) {
                StageFloor()
                SpeechBubble(
                    text = "또 들으니까 신나!",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 110.dp),
                )
                DancingMascot(
                    character = friend,
                    positionMs = positionMs,
                    bpm = song.result.bpm,
                    firstBeatMs = song.result.firstBeatMs,
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 250.dp)
                        .width(520.dp),
                )
                FriendChip(
                    onClick = onChangeFriend,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp),
                )
                HintChip(
                    text = "${friend.friendName.josa("이", "가")} 우리 노래에 맞춰 춤을 춰요",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 44.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("오늘 만든 노래", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
                    Text(song.result.title, style = AppTypography.HeadlineLarge, color = AppColors.TextDarkPrimary, maxLines = 1)
                }
                LyricsBox(
                    lines = song.result.lyrics,
                    positionMs = positionMs,
                    onSeek = onSeek,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
                PlayerBar(isPlaying = isPlaying, positionMs = positionMs, durationMs = durationMs, onToggle = onToggle, onSeek = onSeek, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    LargeButton(
                        text = if (saving) "저장하는 중" else "보관함에 저장",
                        onClick = onSave,
                        enabled = !saving,
                        icon = R.drawable.ic_check,
                        modifier = Modifier.weight(1f),
                    )
                    LargeButton(
                        text = "한 곡 더",
                        onClick = onMakeAnother,
                        icon = R.drawable.ic_mic,
                        variant = LargeButtonVariant.Secondary,
                        modifier = Modifier.width(340.dp),
                    )
                }
            }
        }
    }
}

/** 스테이지 오른쪽 위 '친구 바꾸기' (시안 S06 확대 화면의 버튼). */
@Composable
private fun FriendChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(ChipShape)
            .background(AppColors.SurfaceBasic)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(AppColors.BrandOrange),
        )
        Text("친구 바꾸기", style = AppTypography.BodySmallStrong, color = AppColors.TextDarkSecondary)
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun CompletedScreenPreview() {
    val result = com.hmgappchallenge.kidsrhythm.server.FakeSongService.SAMPLE_RESULT
    CompletedScreen(
        song = CompletedSong("preview", result, java.io.File(""), 0L),
        friend = MascotCharacter.CAT,
        isPlaying = true,
        positionMs = 5_000,
        durationMs = result.durationMs,
        saving = false,
        onToggle = {},
        onSeek = {},
        onSave = {},
        onMakeAnother = {},
        onChangeFriend = {},
        onBack = {},
    )
}
