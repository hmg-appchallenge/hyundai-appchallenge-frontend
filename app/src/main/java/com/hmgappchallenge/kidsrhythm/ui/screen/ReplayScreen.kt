package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.hmgappchallenge.kidsrhythm.beat.speedZoneForBpm
import com.hmgappchallenge.kidsrhythm.beat.beatPositionAt
import com.hmgappchallenge.kidsrhythm.ui.component.DancingCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentBadge
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.LyricsPeek
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.WarmConfirmPopup
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerTicker
import com.hmgappchallenge.kidsrhythm.ui.component.SongLyricsList
import com.hmgappchallenge.kidsrhythm.ui.component.SongMiniPlayer
import com.hmgappchallenge.kidsrhythm.ui.component.SongProgressPlayer
import com.hmgappchallenge.kidsrhythm.ui.component.StageFloor
import com.hmgappchallenge.kidsrhythm.ui.component.StageViewButton
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.component.currentLyricIndex
import com.hmgappchallenge.kidsrhythm.ui.component.lyricText
import com.hmgappchallenge.kidsrhythm.ui.component.mascotAnimationFor
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.launch

private val StageShape = RoundedCornerShape(48.dp)

/** 정차 중 무대 높이와 친구 위치. */
private val StageHeight = 507.dp
private val StageFloorTop = 364.dp
private val CharacterTop = 84.dp
private val CharacterHeight = 351.dp

// 크게 보기는 S06 크게 보기와 같은 무대 크기를 쓴다
private val BigStageHeight = 598.dp
private val BigFloorTop = 480.dp
private val BigCharacterTop = 103.dp
private val BigCharacterHeight = 430.dp

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
            StoppedReplay(song, player, character, onNext = onNext, onOtherSongs = onBack, onDelete = { confirmDelete = true })
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
 * 정차 중: 왼쪽 무대에서 친구가 춤추고, 오른쪽에 가사 접기·다른 노래 듣기·지우기.
 * 아래에 지난 줄·지금 줄·다음 두 줄과 미니 플레이어를 둔다.
 * 가사 접기는 가사 줄을 숨기고, 크게 보기는 S06 크게 보기처럼 무대를 넓힌다.
 */
@Composable
private fun ColumnScope.StoppedReplay(
    song: ArchivedSong,
    player: SongPlayer,
    character: MascotCharacter,
    onNext: () -> Unit,
    onOtherSongs: () -> Unit,
    onDelete: () -> Unit,
) {
    var lyricsFolded by rememberSaveable { mutableStateOf(false) }
    var enlarged by rememberSaveable { mutableStateOf(false) }
    if (enlarged) {
        EnlargedReplay(song, player, character, onShrink = { enlarged = false }, onNext = onNext, onOtherSongs = onOtherSongs, onDelete = onDelete)
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (lyricsFolded) Modifier.weight(1f) else Modifier.height(StageHeight)),
        horizontalArrangement = Arrangement.spacedBy(48.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(StageShape)
                .background(AppColors.CompletedStageBackground),
        ) {
            StageFloor(top = StageFloorTop)
            DancingCharacter(
                character,
                animation = mascotAnimationFor(character, speedZoneForBpm(song.bpm)),
                top = CharacterTop,
                height = CharacterHeight,
                beat = if (player.isPlaying) beatPositionAt(player.positionMs, song.bpm, song.firstBeatMs) else null,
            )
            StageViewButton(
                text = "크게 보기",
                icon = R.drawable.ic_expand,
                onClick = { enlarged = true },
                modifier = Modifier.offset(x = 39.dp, y = 36.dp),
            )
        }
        Column(modifier = Modifier.width(700.dp)) {
            Spacer(Modifier.height(94.dp))
            Text(
                "가사 줄을 누르면 그 부분부터 들어요",
                style = AppTypography.LabelMedium,
                color = AppColors.ReplayHint,
                modifier = Modifier.align(Alignment.End),
            )
            Spacer(Modifier.height(13.dp))
            LargeButton(
                text = if (lyricsFolded) "가사 보기" else "가사 접기",
                onClick = { lyricsFolded = !lyricsFolded },
                icon = if (lyricsFolded) R.drawable.ic_expand else R.drawable.ic_shrink,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(58.dp))
            LargeButton(text = "다른 노래 듣기", onClick = onOtherSongs, icon = R.drawable.ic_music, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            LargeButton(
                text = "지우기",
                onClick = onDelete,
                icon = R.drawable.ic_trash,
                variant = LargeButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    if (!lyricsFolded) {
        CurrentLyrics(song, player, modifier = Modifier.padding(top = 30.dp).fillMaxWidth().height(232.dp))
        Spacer(Modifier.weight(1f))
    }
    ReplayMiniPlayer(song, player, onNext, modifier = Modifier.padding(top = 38.dp).fillMaxWidth())
}

/** 정차 중 크게 보기: 무대를 가로로 넓히고, 아래에 다른 노래 듣기·지우기와 미니 플레이어를 둔다. */
@Composable
private fun ColumnScope.EnlargedReplay(
    song: ArchivedSong,
    player: SongPlayer,
    character: MascotCharacter,
    onShrink: () -> Unit,
    onNext: () -> Unit,
    onOtherSongs: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BigStageHeight)
            .clip(StageShape)
            .background(AppColors.CompletedStageBackground),
    ) {
        StageFloor(top = BigFloorTop)
        DancingCharacter(
            character,
            animation = mascotAnimationFor(character, speedZoneForBpm(song.bpm)),
            top = BigCharacterTop,
            height = BigCharacterHeight,
            beat = if (player.isPlaying) beatPositionAt(player.positionMs, song.bpm, song.firstBeatMs) else null,
        )
        StageViewButton(
            text = "작게 보기",
            icon = R.drawable.ic_shrink,
            onClick = onShrink,
            modifier = Modifier.offset(x = 39.dp, y = 36.dp),
        )
    }
    ReplayActions(onOtherSongs, onDelete, modifier = Modifier.padding(top = 32.dp).fillMaxWidth())
    Spacer(Modifier.weight(1f))
    ReplayMiniPlayer(song, player, onNext, modifier = Modifier.fillMaxWidth())
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

/** 다시 듣기의 가사 몇 줄. 줄을 누르면 그 부분부터 듣는다. */
@Composable
private fun CurrentLyrics(song: ArchivedSong, player: SongPlayer, modifier: Modifier = Modifier) {
    val lines = song.lyricLines
    LyricsPeek(
        lines = lines.map { it.text },
        current = currentLyricIndex(lines, player.positionMs),
        activeText = { lyricText(lines[it]) },
        onLineClick = { player.seekTo(lines[it].startMs) },
        modifier = modifier,
    )
}

@Composable
private fun ReplayActions(onOtherSongs: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        LargeButton(text = "다른 노래 듣기", onClick = onOtherSongs, icon = R.drawable.ic_music, modifier = Modifier.weight(1f))
        LargeButton(
            text = "지우기",
            onClick = onDelete,
            icon = R.drawable.ic_trash,
            variant = LargeButtonVariant.Secondary,
            // 시안 너비 242에서는 글자가 잘릴 수 있어, 다 보이는 만큼만 넓힌다
            modifier = Modifier.widthIn(min = 242.dp),
        )
    }
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
