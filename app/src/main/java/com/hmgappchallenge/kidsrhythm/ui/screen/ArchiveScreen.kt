package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
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
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerTicker
import com.hmgappchallenge.kidsrhythm.ui.component.SongRow
import com.hmgappchallenge.kidsrhythm.ui.component.TabPage
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.itemsIndexed
import com.hmgappchallenge.kidsrhythm.ui.component.SongMiniPlayer
import com.hmgappchallenge.kidsrhythm.ui.component.songTone

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
        headerActions = {
            if (songs.isEmpty()) Text("전체 노래 0곡", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
        },
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
            EmptyArchive(onOpenMake)
            return@TabPage
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Spacer(Modifier.weight(1f))
            Text("노래 ${songs.size}곡", style = AppTypography.LabelMedium, color = AppColors.SongMeta)
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                Box(
                    modifier = Modifier.pointerInput(song.id) {
                        detectTapGestures(onTap = { onOpenSong(song.id) }, onLongPress = { deleting = song })
                    },
                ) {
                    SongRow(
                        title = song.title,
                        meta = songMetaLabel(song, now),
                        onPlay = { player.load(song.audioFilePath, autoplay = true, knownDurationMs = song.durationMillis) },
                        tone = songTone(index),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                Text(
                    "길게 누르면 노래를 지울 수 있어요",
                    style = AppTypography.LabelMedium,
                    color = AppColors.HintChipText,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
        if (playing != null) {
            val index = songs.indexOf(playing)
            val duration = player.durationMs.takeIf { it > 0 } ?: playing.durationMillis
            // 줄 ▶는 목록에서 바로 틀고, 미니 플레이어를 누르면 그 곡의 다시 듣기로 간다
            SongMiniPlayer(
                title = playing.title,
                progress = if (duration > 0) player.positionMs.toFloat() / duration else 0f,
                isPlaying = player.isPlaying,
                onTogglePlay = player::toggle,
                onNext = {
                    val next = songs[(index + 1) % songs.size]
                    player.load(next.audioFilePath, autoplay = true, knownDurationMs = next.durationMillis)
                },
                tone = songTone(index),
                onClick = { onOpenSong(playing.id) },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

/** S07-0 빈 보관함. 가운데에 음표와 안내, 노래 만들러 가기 버튼. */
@Composable
private fun EmptyArchive(onOpenMake: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(AppColors.EmptyArchiveCircle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_music), contentDescription = null, tint = AppColors.EmptyArchiveIcon, modifier = Modifier.size(96.dp))
        }
        Text("아직 만든 노래가 없어요", style = AppTypography.HeadlineSmall, color = AppColors.TextDarkPrimary)
        Text("차를 타고 가면서 가족과 첫 노래를 만들어볼까요?", style = AppTypography.BodyLargeStrong.copy(fontWeight = FontWeight.Normal), color = AppColors.TextDarkSecondary)
        LargeButton(
            text = "노래 만들러 가기",
            onClick = onOpenMake,
            icon = R.drawable.ic_play,
            variant = LargeButtonVariant.Filled,
            modifier = Modifier.width(508.dp),
        )
    }
}
