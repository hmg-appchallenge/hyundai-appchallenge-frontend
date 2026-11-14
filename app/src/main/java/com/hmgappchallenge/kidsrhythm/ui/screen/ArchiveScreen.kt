package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import com.hmgappchallenge.kidsrhythm.family.subjectParticle
import com.hmgappchallenge.kidsrhythm.ui.component.IconToast
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import kotlinx.coroutines.delay
import com.hmgappchallenge.kidsrhythm.family.objectParticle
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
import com.hmgappchallenge.kidsrhythm.ui.component.WarmConfirmPopup
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerTicker
import com.hmgappchallenge.kidsrhythm.ui.component.SongRow
import com.hmgappchallenge.kidsrhythm.ui.component.TabPage
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.itemsIndexed
import com.hmgappchallenge.kidsrhythm.ui.component.SongMiniPlayer
import com.hmgappchallenge.kidsrhythm.ui.component.songTone

/** 노래를 지운 뒤 안내를 보여 주는 시간. */
private const val DELETED_NOTICE_MILLIS = 2_000L

/** 지울 곡 확인 팝업에 이름을 적는 곡 수. 더 많으면 "외 N곡"으로 줄인다. */
private const val NAMED_SONGS_IN_CONFIRM = 2

/**
 * S07 보관함. 노래 목록, 아래 미니 플레이어. 줄을 누르면 다시 듣기로 간다.
 * 한 곡은 길게 눌러 지우고, 여러 곡은 "노래 지우기"로 골라 한 번에 지운다.
 */
@Composable
fun ArchiveRoute(services: AppServices, onOpenMake: () -> Unit, onOpenSong: (Long) -> Unit) {
    val songs by services.archive.songs.collectAsState()
    val player = services.player
    val scope = rememberCoroutineScope()
    var deleting by remember { mutableStateOf<ArchivedSong?>(null) }
    var selecting by rememberSaveable { mutableStateOf(false) }
    // 누른 차례대로 둔다. 확인 팝업에 먼저 고른 곡 이름부터 적는다
    var selectedIds by rememberSaveable { mutableStateOf(listOf<Long>()) }
    var deletedNotice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { services.beatPlayer.stop() }
    LaunchedEffect(deletedNotice) {
        if (deletedNotice != null) {
            delay(DELETED_NOTICE_MILLIS)
            deletedNotice = null
        }
    }
    PlayerTicker(player)
    val playing = songs.firstOrNull { it.audioFilePath == player.path }
    val now = System.currentTimeMillis()

    fun deleteSongs(targets: List<ArchivedSong>) {
        if (targets.any { it.audioFilePath == player.path }) player.release()
        scope.launch { targets.forEach { services.archive.delete(it.id) } }
    }

    if (selecting) {
        BackHandler { selecting = false }
        SelectSongsToDelete(
            songs = songs,
            selectedIds = selectedIds.filter { id -> songs.any { it.id == id } },
            now = now,
            onToggle = { id -> selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id },
            onToggleAll = {
                val allIds = songs.map { it.id }
                selectedIds = if (selectedIds.containsAll(allIds)) emptyList() else selectedIds + (allIds - selectedIds.toSet())
            },
            onClose = { selecting = false },
            onDelete = { targets ->
                deleteSongs(targets)
                deletedNotice = "노래 ${targets.size}곡을 지웠어요"
                selecting = false
            },
        )
        return
    }

    TabPage(
        selectedTab = AppTab.Archive,
        headerActions = {
            if (songs.isEmpty()) {
                Text("전체 노래 0곡", style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary)
            } else {
                LargeButton(
                    text = "노래 지우기",
                    onClick = {
                        selectedIds = emptyList()
                        selecting = true
                    },
                    icon = R.drawable.ic_trash,
                    variant = LargeButtonVariant.Secondary,
                    modifier = Modifier.width(336.dp),
                )
            }
        },
        onSelectTab = { if (it == AppTab.Make) onOpenMake() },
        overlay = {
            deleting?.let { song ->
                WarmConfirmPopup(
                    title = "'${song.title}'${objectParticle(song.title)} 지울까요?",
                    message = "이 차에서 노래가 지워지고, 지우면 되돌릴 수 없어요.",
                    cancelText = "취소",
                    onCancel = { deleting = null },
                    confirmText = "지우기",
                    onConfirm = {
                        deleteSongs(listOf(song))
                        deleting = null
                    },
                    confirmIcon = R.drawable.ic_trash,
                    confirmVariant = LargeButtonVariant.Primary,
                )
            }
            deletedNotice?.let { IconToast(R.drawable.ic_trash, it) }
        },
    ) {
        if (songs.isEmpty()) {
            EmptyArchive(onOpenMake)
            return@TabPage
        }
        SongCount(songs.size)
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

@Composable
private fun SongCount(count: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Spacer(Modifier.weight(1f))
        Text("노래 ${count}곡", style = AppTypography.LabelMedium, color = AppColors.SongMeta)
    }
}

/** S07 지울 노래 고르기. 줄을 눌러 고르고, 아래 버튼으로 한 번 더 물은 뒤 지운다. */
@Composable
private fun SelectSongsToDelete(
    songs: List<ArchivedSong>,
    selectedIds: List<Long>,
    now: Long,
    onToggle: (Long) -> Unit,
    onToggleAll: () -> Unit,
    onClose: () -> Unit,
    onDelete: (List<ArchivedSong>) -> Unit,
) {
    var confirming by remember { mutableStateOf(false) }
    val targets = selectedIds.mapNotNull { id -> songs.firstOrNull { it.id == id } }
    SubPage(
        title = "지울 노래를 골라요",
        onBack = onClose,
        backIcon = R.drawable.ic_close,
        backDescription = "그만 고르기",
        headerActions = {
            LargeButton(
                text = "전체 선택하기",
                onClick = onToggleAll,
                icon = R.drawable.ic_check,
                variant = LargeButtonVariant.Secondary,
                modifier = Modifier.width(336.dp),
            )
        },
        overlay = {
            if (confirming && targets.isNotEmpty()) {
                WarmConfirmPopup(
                    title = "노래 ${targets.size}곡을 지울까요?",
                    message = "${deleteTargetsLabel(targets.map { it.title })} 이 차에서 지워지고,\n지우면 되돌릴 수 없어요.",
                    cancelText = "그만 두기",
                    onCancel = { confirming = false },
                    confirmText = "지우기",
                    onConfirm = {
                        confirming = false
                        onDelete(targets)
                    },
                    confirmIcon = R.drawable.ic_trash,
                )
            }
        },
    ) {
        SongCount(songs.size)
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                val selected = song.id in selectedIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SelectRowShape)
                        .clickable(role = Role.Checkbox, onClick = { onToggle(song.id) }),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SongRow(
                        title = song.title,
                        meta = songMetaLabel(song, now),
                        onPlay = null,
                        tone = songTone(index),
                        selected = selected,
                        modifier = Modifier.weight(1f),
                    )
                    SelectCircle(selected)
                }
            }
        }
        DeleteBar(count = targets.size, onDelete = { confirming = true })
    }
}

private val SelectRowShape = RoundedCornerShape(32.dp)
private val DeleteBarShape = RoundedCornerShape(32.dp)

/** 고른 곡 옆 동그라미. 고르면 주황 바탕에 체크가 보인다. */
@Composable
private fun SelectCircle(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(if (selected) AppColors.BrandOrange else AppColors.SpeedChipBackground)
            .then(if (selected) Modifier.border(6.dp, AppColors.SpeedChipBackground, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(painterResource(R.drawable.ic_check), contentDescription = "고름", tint = AppColors.SnackbarContent, modifier = Modifier.size(46.dp))
        }
    }
}

/** 고르기 화면 아래 안내와 지우기 버튼. 고른 곡이 없으면 버튼을 잠근다. */
@Composable
private fun DeleteBar(count: Int, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth()
            .clip(DeleteBarShape)
            .background(AppColors.SurfaceBasic)
            .border(2.dp, AppColors.SpeedChipBackground, DeleteBarShape)
            .padding(horizontal = 26.dp, vertical = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "지울 노래를 눌러 골라요",
            style = AppTypography.TitleSmall,
            color = AppColors.SongMeta,
            modifier = Modifier.weight(1f).padding(start = 40.dp),
        )
        LargeButton(
            text = "선택 노래 지우기",
            onClick = onDelete,
            icon = R.drawable.ic_trash,
            enabled = count > 0,
            modifier = Modifier.weight(1f),
        )
    }
}

/** 확인 팝업에 적을 지울 곡 이름. 예: "'둥둥 노래', '기차 노래'가", "'사과 노래', '기차 노래' 외 2곡이" */
internal fun deleteTargetsLabel(titles: List<String>): String {
    val named = titles.take(NAMED_SONGS_IN_CONFIRM).joinToString(", ") { "'$it'" }
    val rest = titles.size - NAMED_SONGS_IN_CONFIRM
    return if (rest > 0) "$named 외 ${rest}곡이" else "$named${subjectParticle(titles.last())}"
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
