package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.AppServices
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.archive.ArchivedSong
import com.hmgappchallenge.kidsrhythm.archive.songMetaLabel
import com.hmgappchallenge.kidsrhythm.ui.component.AppTab
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.MessagePopup
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerBar
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerTicker
import com.hmgappchallenge.kidsrhythm.ui.component.SongRow
import com.hmgappchallenge.kidsrhythm.ui.component.TabPage
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlinx.coroutines.launch

private val MiniPlayerShape = RoundedCornerShape(32.dp)

/** S07 보관함. 노래 목록, 길게 눌러 지우기, 아래 미니 플레이어. 줄을 누르면 다시 듣기로 간다. */
@Composable
fun ArchiveRoute(services: AppServices, onOpenMake: () -> Unit, onOpenSong: (Long) -> Unit) {
    val songs by services.archive.songs.collectAsState()
    val player = services.player
    val scope = rememberCoroutineScope()
    var deleting by remember { mutableStateOf<ArchivedSong?>(null) }
    LaunchedEffect(Unit) { services.beatPlayer.stop() }
    PlayerTicker(player)
    val playing = songs.firstOrNull { it.audioFilePath == player.path }
    val now = System.currentTimeMillis()

    TabPage(
        selectedTab = AppTab.Archive,
        onSelectTab = { if (it == AppTab.Make) onOpenMake() },
        overlay = {
            deleting?.let { song ->
                MessagePopup(
                    title = "노래를 지울까요?",
                    message = "'${song.title}'을 지우면 다시 들을 수 없어요.",
                    primaryText = "지우기",
                    onPrimary = {
                        if (player.path == song.audioFilePath) player.release()
                        scope.launch { services.archive.delete(song.id) }
                        deleting = null
                    },
                    secondaryText = "취소",
                    onSecondary = { deleting = null },
                    primaryIcon = R.drawable.ic_trash,
                    secondaryIcon = R.drawable.ic_close,
                    destructive = true,
                )
            }
        },
    ) {
        if (songs.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("아직 만든 노래가 없어요", style = AppTypography.TitleLarge, color = AppColors.TextDarkPrimary)
                Text("차가 달리는 리듬으로 첫 노래를 만들어 볼까요?", style = AppTypography.TitleMediumNormal, color = AppColors.TextDarkTertiary)
                LargeButton(text = "노래 만들기", onClick = onOpenMake, icon = R.drawable.ic_play, modifier = Modifier.width(480.dp))
            }
            return@TabPage
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Spacer(Modifier.weight(1f))
            Text("노래 ${songs.size}곡", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            items(songs, key = { it.id }) { song ->
                Box(
                    modifier = Modifier.pointerInput(song.id) {
                        detectTapGestures(onTap = { onOpenSong(song.id) }, onLongPress = { deleting = song })
                    },
                ) {
                    SongRow(
                        title = song.title,
                        meta = songMetaLabel(song, now),
                        onPlay = { player.load(song.audioFilePath, autoplay = true, knownDurationMs = song.durationMillis) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                Text(
                    "길게 누르면 노래를 지울 수 있어요",
                    style = AppTypography.LabelMedium,
                    color = AppColors.TextDarkTertiary,
                    modifier = Modifier.padding(top = 12.dp, start = 8.dp),
                )
            }
        }
        if (playing != null) {
            PlayerBar(
                isPlaying = player.isPlaying,
                positionMs = player.positionMs,
                durationMs = player.durationMs,
                onToggle = player::toggle,
                onSeek = player::seekTo,
                title = playing.title,
                onNext = {
                    val i = songs.indexOf(playing)
                    val next = songs[(i + 1) % songs.size]
                    player.load(next.audioFilePath, autoplay = true, knownDurationMs = next.durationMillis)
                },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .clip(MiniPlayerShape)
                    .background(AppColors.SurfaceBasic)
                    .border(2.dp, AppColors.BasicAlphaLight50, MiniPlayerShape)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}
