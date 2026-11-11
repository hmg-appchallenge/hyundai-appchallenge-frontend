package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.AppServices
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.archive.ArchivedMember
import com.hmgappchallenge.kidsrhythm.archive.durationLabel
import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentBadge
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.LyricsBox
import com.hmgappchallenge.kidsrhythm.ui.component.MessagePopup
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerBar
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerTicker
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.launch

/** S07 다시 듣기. 함께한 가족과 악기, 가사(지금 부르는 줄 강조, 줄 눌러 듣기), 재생, 지우기. */
@Composable
fun ReplayRoute(services: AppServices, songId: Long, onBack: () -> Unit) {
    val songs by services.archive.songs.collectAsState()
    val song = songs.firstOrNull { it.id == songId }
    val player = services.player
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }
    if (song == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    LaunchedEffect(song.id) {
        services.beatPlayer.stop()
        player.load(song.audioFilePath, autoplay = true, knownDurationMs = song.durationMillis)
    }
    PlayerTicker(player)
    val created = Instant.ofEpochMilli(song.createdAtMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    SubPage(
        title = "다시 듣기",
        onBack = onBack,
        overlay = {
            if (confirmDelete) {
                MessagePopup(
                    title = "노래를 지울까요?",
                    message = "'${song.title}'을 지우면 다시 들을 수 없어요.",
                    primaryText = "지우기",
                    onPrimary = {
                        player.release()
                        scope.launch {
                            services.archive.delete(song.id)
                            onBack()
                        }
                    },
                    secondaryText = "취소",
                    onSecondary = { confirmDelete = false },
                    primaryIcon = R.drawable.ic_trash,
                    secondaryIcon = R.drawable.ic_close,
                    destructive = true,
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${created.monthValue}/${created.dayOfMonth} 만든 노래 · ${durationLabel(song.durationMillis)}", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
            Text(song.title, style = AppTypography.HeadlineLarge, color = AppColors.TextDarkPrimary, maxLines = 1)
        }
        if (song.members.isNotEmpty()) {
            Row(modifier = Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                song.members.forEach { MemberChip(it) }
            }
        }
        LyricsBox(
            lines = song.lyricLines,
            positionMs = player.positionMs,
            onSeek = player::seekTo,
            modifier = Modifier
                .padding(top = 24.dp)
                .weight(1f)
                .fillMaxWidth(),
        )
        PlayerBar(
            isPlaying = player.isPlaying,
            positionMs = player.positionMs,
            durationMs = player.durationMs.takeIf { it > 0 } ?: song.durationMillis,
            onToggle = player::toggle,
            onSeek = player::seekTo,
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
        )
        Row(modifier = Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            LargeButton(text = "다른 노래 듣기", onClick = onBack, icon = R.drawable.ic_music, modifier = Modifier.weight(1f))
            LargeButton(
                text = "지우기",
                onClick = { confirmDelete = true },
                icon = R.drawable.ic_trash,
                variant = LargeButtonVariant.Secondary,
                modifier = Modifier.width(320.dp),
            )
        }
    }
}

@Composable
private fun MemberChip(member: ArchivedMember) {
    val instrument = Instrument.entries.firstOrNull { it.name == member.instrument }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(AppColors.SurfaceBasic)
            .padding(start = 8.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (instrument != null) InstrumentBadge(instrument, size = 56.dp)
        if (member.name.isNotEmpty()) Text(member.name, style = AppTypography.BodySmallStrong, color = AppColors.TextDarkPrimary)
        Text(instrument?.label ?: member.instrument, style = AppTypography.BodySmallStrong, color = AppColors.TextDarkTertiary)
    }
}
