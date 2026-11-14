package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import com.hmgappchallenge.kidsrhythm.ui.component.LyricsFocusLayout
import com.hmgappchallenge.kidsrhythm.ui.component.BigStageLayout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.AppServices
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.archive.ArchivedMember
import com.hmgappchallenge.kidsrhythm.archive.ArchivedSong
import com.hmgappchallenge.kidsrhythm.archive.durationLabel
import com.hmgappchallenge.kidsrhythm.audio.SongPlayer
import com.hmgappchallenge.kidsrhythm.beat.beatPositionAt
import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentBadge
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.WarmConfirmPopup
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerTicker
import com.hmgappchallenge.kidsrhythm.ui.component.SongLyricsList
import com.hmgappchallenge.kidsrhythm.ui.component.SongMiniPlayer
import com.hmgappchallenge.kidsrhythm.ui.component.SongProgressPlayer
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.component.currentLyricIndex
import com.hmgappchallenge.kidsrhythm.ui.component.lyricText
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.launch

private val StageShape = RoundedCornerShape(48.dp)

/**
 * S07-1 다시 듣기. 주행 중에는 친구 없이 제목·가족·가사·재생 막대만 보여 주고(CarUxRestrictions),
 * 정차 중에는 무대에서 친구가 춤추고 아래에 지금 가사 몇 줄과 미니 플레이어를 보여 준다.
 */
@Composable
fun ReplayRoute(
    services: AppServices,
    songId: Long,
    driving: Boolean,
    character: MascotCharacter,
    onBack: () -> Unit,
    onOpenSong: (Long) -> Unit,
    onChangeCharacter: () -> Unit,
    startEnlarged: Boolean = true,
) {
    val songs by services.archive.songs.collectAsState()
    val song = songs.firstOrNull { it.id == songId }
    val player = services.player
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }
    // 지우는 중에는 목록에서 곡이 사라져도 여기서 또 뒤로 가지 않는다 (지우기가 직접 한 번만 뒤로 간다)
    var deleting by remember { mutableStateOf(false) }
    if (song == null) {
        if (!deleting) LaunchedEffect(Unit) { onBack() }
        return
    }
    LaunchedEffect(song.id) {
        services.beatPlayer.stop()
        player.load(song.audioFilePath, autoplay = true, knownDurationMs = song.durationMillis)
    }
    PlayerTicker(player)
    val onNext = {
        val next = songs[(songs.indexOf(song) + 1) % songs.size]
        if (next.id != song.id) onOpenSong(next.id) else player.seekTo(0)
    }
    val overlay: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit = {
        if (confirmDelete) {
            WarmConfirmPopup(
                title = "저장된 노래를 지울까요?",
                message = "삭제한 노래는 복구할 수 없으며,\n삭제 후에는 다시 이용할 수 없습니다.",
                cancelText = "취소",
                onCancel = { confirmDelete = false },
                confirmText = "지우기",
                onConfirm = {
                    if (!deleting) {
                        deleting = true
                        player.release()
                        scope.launch {
                            services.archive.delete(song.id)
                            onBack()
                        }
                    }
                },
                confirmIcon = R.drawable.ic_trash,
            )
        }
    }
    if (driving) {
        SubPage(title = "다시 듣기", onBack = onBack, overlay = overlay) {
            DrivingReplay(song, player, onNext = onNext, onOtherSongs = onBack, onDelete = { confirmDelete = true })
        }
    } else {
        SubPage(
            title = "다시 듣기",
            onBack = onBack,
            overlay = overlay,
            headerActions = {
                LargeButton(
                    text = "친구 바꾸기",
                    onClick = onChangeCharacter,
                    icon = R.drawable.ic_paw,
                    variant = LargeButtonVariant.Secondary,
                    modifier = Modifier.widthIn(min = 279.dp),
                )
            },
        ) {
            StoppedReplay(song, player, character, startEnlarged, onNext = onNext, onOtherSongs = onBack, onDelete = { confirmDelete = true })
        }
    }
}

/** 주행 중: 친구 없이 제목·가족·가사 전체·재생 막대. 가사 접기를 누르면 가사를 숨긴다. */
@Composable
private fun ColumnScope.DrivingReplay(song: ArchivedSong, player: SongPlayer, onNext: () -> Unit, onOtherSongs: () -> Unit, onDelete: () -> Unit) {
    var lyricsFolded by rememberSaveable { mutableStateOf(false) }
    val created = Instant.ofEpochMilli(song.createdAtMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val lines = song.lyricLines
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(32.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${created.monthValue}/${created.dayOfMonth} 만든 노래 · ${durationLabel(song.durationMillis)}",
                style = AppTypography.LabelMedium,
                color = AppColors.SongMeta,
            )
            Text(song.title, style = AppTypography.HeadlineLarge, color = AppColors.TextDarkPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (song.members.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                song.members.forEach { MemberChip(it) }
            }
        }
        if (lyricsFolded) {
            Spacer(Modifier.weight(1f))
        } else {
            SongLyricsList(
                lines = lines.map { it.text },
                current = currentLyricIndex(lines, player.positionMs).coerceAtLeast(0),
                sections = lines.map { it.section },
                activeText = { lyricText(lines[it]) },
                onLineClick = { player.seekTo(lines[it].startMs) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
        SongProgressPlayer(
            isPlaying = player.isPlaying,
            positionMillis = player.positionMs,
            durationMillis = player.durationMs.takeIf { it > 0 } ?: song.durationMillis,
            onTogglePlay = player::toggle,
            onSeek = player::seekTo,
            onNext = onNext,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Row(modifier = Modifier.padding(top = 31.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        LargeButton(text = "다른 노래 듣기", onClick = onOtherSongs, icon = R.drawable.ic_music, modifier = Modifier.weight(1f))
        LargeButton(
            text = if (lyricsFolded) "가사 보기" else "가사 접기",
            onClick = { lyricsFolded = !lyricsFolded },
            icon = if (lyricsFolded) R.drawable.ic_expand else R.drawable.ic_shrink,
            variant = LargeButtonVariant.Outlined,
            modifier = Modifier.weight(1f),
        )
        LargeButton(
            text = "지우기",
            onClick = onDelete,
            icon = R.drawable.ic_trash,
            variant = LargeButtonVariant.Secondary,
            modifier = Modifier.widthIn(min = 242.dp),
        )
    }
}

/**
 * 정차 중: S06 크게 보기와 같은 모습. 처음에는 넓은 무대 아래에 주황 가사 막대를 두고,
 * 막대를 누르면 무대를 가운데로 줄이고 가사 몇 줄을 펼친다. 아래에 미니 플레이어와 다른 노래 듣기 · 지우기.
 */
@Composable
private fun ColumnScope.StoppedReplay(
    song: ArchivedSong,
    player: SongPlayer,
    character: MascotCharacter,
    startEnlarged: Boolean,
    onNext: () -> Unit,
    onOtherSongs: () -> Unit,
    onDelete: () -> Unit,
) {
    // 설정이 크게 보기면 넓은 무대로, 기본 보기면 무대를 줄이고 가사를 펼친 모습으로 시작한다
    var lyricsOpen by rememberSaveable { mutableStateOf(!startEnlarged) }
    val lines = song.lyricLines
    val current = currentLyricIndex(lines, player.positionMs)
    val beat = if (player.isPlaying) beatPositionAt(player.positionMs, song.bpm, song.firstBeatMs) else null
    val miniPlayer: @Composable () -> Unit = { ReplayMiniPlayer(song, player, onNext, modifier = Modifier.fillMaxWidth()) }
    val actions: @Composable RowScope.() -> Unit = { ReplayActions(onOtherSongs, onDelete) }
    if (lyricsOpen) {
        LyricsFocusLayout(
            character = character,
            bpm = song.bpm,
            beat = beat,
            lines = lines.map { it.text },
            current = current,
            activeText = { lyricText(lines[it]) },
            onLineClick = { player.seekTo(lines[it].startMs) },
            onFold = { lyricsOpen = false },
            player = miniPlayer,
            actions = actions,
        )
    } else {
        // 첫 줄 전이면 첫 줄을 미리 보여 준다
        val line = lines.getOrNull(current.coerceAtLeast(0))
        BigStageLayout(
            character = character,
            bpm = song.bpm,
            beat = beat,
            lyric = line?.let { lyricText(it, AppColors.LyricBarHighlight) } ?: AnnotatedString(""),
            // 다시 듣기에는 작게 볼 다른 화면이 없어서, 작게 보기는 가사를 펼친 모습으로 간다
            onShrink = { lyricsOpen = true },
            onOpenLyrics = { lyricsOpen = true },
            player = miniPlayer,
            actions = actions,
        )
    }
}

@Composable
private fun ReplayMiniPlayer(song: ArchivedSong, player: SongPlayer, onNext: () -> Unit, modifier: Modifier = Modifier) {
    val duration = player.durationMs.takeIf { it > 0 } ?: song.durationMillis
    SongMiniPlayer(
        title = song.title,
        subtitle = song.members.joinToString(" · ") { memberLabel(it) },
        progress = if (duration > 0) player.positionMs.toFloat() / duration else 0f,
        isPlaying = player.isPlaying,
        onTogglePlay = player::toggle,
        onNext = onNext,
        buttonSize = 63.dp,
        onSeek = { if (duration > 0) player.seekTo((it * duration).toLong()) },
        modifier = modifier,
    )
}

/** 오른쪽 아래의 다른 노래 듣기 · 지우기. */
@Composable
private fun ReplayActions(onOtherSongs: () -> Unit, onDelete: () -> Unit) {
    // 320에서는 글자가 잘려, 다 보이는 만큼 넓힌다
    LargeButton(text = "다른 노래 듣기", onClick = onOtherSongs, icon = R.drawable.ic_music, modifier = Modifier.widthIn(min = 320.dp))
    LargeButton(
        text = "지우기",
        onClick = onDelete,
        icon = R.drawable.ic_trash,
        variant = LargeButtonVariant.Secondary,
        modifier = Modifier.width(320.dp),
    )
}

private fun memberLabel(member: ArchivedMember): String {
    val instrument = Instrument.entries.firstOrNull { it.name == member.instrument }?.label ?: member.instrument
    return "${member.name} $instrument".trim()
}

@Composable
private fun MemberChip(member: ArchivedMember) {
    val instrument = Instrument.entries.firstOrNull { it.name == member.instrument }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(AppColors.StageBubbleBackground)
            .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (instrument != null) InstrumentBadge(instrument, size = 48.dp)
        if (member.name.isNotEmpty()) Text(member.name, style = AppTypography.BodySmallStrong, color = AppColors.TextDarkPrimary)
        Text(instrument?.label ?: member.instrument, style = AppTypography.LabelMedium, color = AppColors.ReplayMemberInstrument)
    }
}
