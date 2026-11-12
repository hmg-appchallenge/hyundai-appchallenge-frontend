package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.completed.CompletedSong
import com.hmgappchallenge.kidsrhythm.completed.Particle
import com.hmgappchallenge.kidsrhythm.completed.ParticleShape
import com.hmgappchallenge.kidsrhythm.completed.formatPlayTime
import com.hmgappchallenge.kidsrhythm.completed.lyricIndexAt
import com.hmgappchallenge.kidsrhythm.completed.nextSpawnDelay
import com.hmgappchallenge.kidsrhythm.beat.speedZoneForBpm
import com.hmgappchallenge.kidsrhythm.completed.randomParticle
import com.hmgappchallenge.kidsrhythm.ui.component.AnimatedMascot
import com.hmgappchallenge.kidsrhythm.ui.component.MascotAnimation
import com.hmgappchallenge.kidsrhythm.ui.component.mascotAnimationFor
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.Mascot
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.MascotPose
import com.hmgappchallenge.kidsrhythm.ui.component.StageFloor
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.component.dropShadow
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import com.hmgappchallenge.kidsrhythm.ui.component.PlayerTicker
import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.completed.completedSongOf
import com.hmgappchallenge.kidsrhythm.archive.ArchivedMember
import com.hmgappchallenge.kidsrhythm.AppServices
import com.hmgappchallenge.kidsrhythm.ui.component.josa
import com.hmgappchallenge.kidsrhythm.ui.component.HintChip
import com.hmgappchallenge.kidsrhythm.ui.component.SpeechBubble
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.gestures.detectTapGestures

private val StageShape = RoundedCornerShape(48.dp)
private val PillShape = RoundedCornerShape(32.dp)
private val TrackShape = RoundedCornerShape(6.dp)
private val PlayerShape = RoundedCornerShape(32.dp)
private val PLAYER_BORDER = 2.dp

private val StageHeight = 598.dp
private val FloorTop = 480.dp

/** 크게 보기에서 앉은 친구가 무대 가운데에 앉는 위치와 높이. 파티클은 이 친구의 가운데에서 퍼진다. */
private val CharacterTop = 103.dp
private val CharacterHeight = 430.dp

/** 기본 보기에서 왼쪽 무대에 앉는 친구의 위치와 높이. */
private val BasicCharacterTop = 287.dp
private val BasicCharacterHeight = 488.dp

/**
 * 애니메이션 프레임(512px, 캐릭터가 위 15px ~ 아래 492px)을 멈춘 이미지와 같은 높이·바닥에 맞춘다.
 * 멈춘 이미지 높이 430일 때 프레임 462, 위치는 14 위로 올린다.
 */
private const val ANIMATION_SIZE_RATIO = 462f / 430f
private const val ANIMATION_LIFT_RATIO = 14f / 430f

/** 기본 보기 패널. */
private val EditButtonShape = RoundedCornerShape(20.dp)
private val LyricsPanelShape = RoundedCornerShape(32.dp)
private val LyricsPanelHeight = 521.dp
private val ProgressShape = RoundedCornerShape(8.dp)

/** 파티클이 생기는 거리. 캐릭터 가운데에서 바로 생기면 몸에 가려 보이지 않아 가장자리쯤에서 시작한다. */
private val ParticleStartRadius = 200.dp

/** 공용 타이포 토큰에 없는 굵기·크기라 여기서만 쓴다. */
private val SongTitleStyle = AppTypography.HeadlineLarge.copy(fontWeight = FontWeight.Bold)
private val ShrinkLabelStyle = AppTypography.TitleMedium.copy(fontWeight = FontWeight.ExtraBold)
private val PlayerMetaStyle = AppTypography.LabelMedium.copy(fontSize = 22.sp)

/** S06 완성. 받아 둔 노래를 바로 틀고, 보관함에 저장하거나 한 곡 더 만든다. */
@Composable
fun CompletedRoute(
    services: AppServices,
    family: List<FamilyMember>,
    character: MascotCharacter,
    onSaved: () -> Unit,
    onMakeAnother: () -> Unit,
    onChangeCharacter: () -> Unit,
    onBack: () -> Unit,
) {
    // 처음 들어올 때의 곡을 붙잡아 둔다. 저장하면서 completedSong을 비워도 이 화면이 '곡 없음'으로 홈에 돌아가지 않게
    val received = remember { services.completedSong }
    if (received == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val song = remember(received) { completedSongOf(received.result, received.createdAtMillis) }
    val membersLabel = remember(received, family) {
        received.result.members.joinToString(" · ") { m ->
            val name = family.firstOrNull { it.id == m.memberId }?.name.orEmpty()
            val instrument = Instrument.entries.firstOrNull { it.name == m.instrument }?.label ?: m.instrument
            "$name $instrument".trim()
        }
    }
    val player = services.player
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    LaunchedEffect(received.songId) {
        services.beatPlayer.stop()
        player.load(received.audioFile.path, autoplay = true, knownDurationMs = received.result.durationMs)
    }
    DisposableEffect(Unit) { onDispose { player.pause() } }
    PlayerTicker(player)
    CompletedScreen(
        song = song,
        character = character,
        membersLabel = membersLabel,
        isPlaying = player.isPlaying,
        positionMillis = player.positionMs,
        saving = saving,
        onTogglePlay = player::toggle,
        onSeek = player::seekTo,
        onBack = onBack,
        onChangeCharacter = onChangeCharacter,
        onSave = {
            if (!saving) {
                saving = true
                scope.launch {
                    val members = received.result.members.map { m ->
                        ArchivedMember(m.memberId, family.firstOrNull { it.id == m.memberId }?.name.orEmpty(), m.instrument)
                    }
                    services.archive.import(received.result, received.audioFile, members, received.createdAtMillis)
                    services.completedSong = null
                    onSaved()
                }
            }
        },
        onMakeAnother = onMakeAnother,
    )
}

/** S06 노래 완성. 고른 친구가 무대에서 춤추고, 아래에서 지금 가사 한 줄과 미니 플레이어를 보여 준다. */
@Composable
fun CompletedScreen(
    song: CompletedSong,
    character: MascotCharacter,
    membersLabel: String,
    isPlaying: Boolean,
    positionMillis: Long,
    saving: Boolean,
    onTogglePlay: () -> Unit,
    onBack: () -> Unit,
    onChangeCharacter: () -> Unit,
    onSave: () -> Unit,
    onMakeAnother: () -> Unit,
    onSeek: (Long) -> Unit = {},
) {
    // 처음에는 기본 보기로 열고, 크게 보기·작게 보기로 오간다
    var enlarged by rememberSaveable { mutableStateOf(false) }
    if (!enlarged) {
        BasicView(
            song = song,
            character = character,
            isPlaying = isPlaying,
            positionMillis = positionMillis,
            saving = saving,
            onTogglePlay = onTogglePlay,
            onSeek = onSeek,
            onEnlarge = { enlarged = true },
            onBack = onBack,
            onSave = onSave,
            onMakeAnother = onMakeAnother,
        )
        return
    }
    val lyric = song.lyrics.getOrNull(lyricIndexAt(song, positionMillis)).orEmpty()

    SubPage(
        title = "완성!",
        onBack = onBack,
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(StageHeight)
                .clip(StageShape)
                .background(AppColors.CompletedStageBackground),
        ) {
            StageFloor(top = FloorTop)
            DancingCharacter(
                character,
                animation = mascotAnimationFor(character, speedZoneForBpm(song.bpm)),
                top = CharacterTop,
                height = CharacterHeight,
            )
            StageViewButton(
                text = "작게 보기",
                icon = R.drawable.ic_shrink,
                onClick = { enlarged = false },
                modifier = Modifier.offset(x = 39.dp, y = 36.dp),
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth()
                .height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LyricLine(lyric, song.highlightWord, modifier = Modifier.weight(1f))
            LargeButton(
                text = if (saving) "저장하는 중" else "보관함에 저장",
                onClick = onSave,
                enabled = !saving,
                icon = R.drawable.ic_save,
                modifier = Modifier.width(320.dp),
            )
            LargeButton(
                text = "한 곡 더",
                onClick = onMakeAnother,
                icon = R.drawable.ic_replay,
                variant = LargeButtonVariant.Secondary,
                modifier = Modifier.width(320.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        MiniPlayer(
            title = song.title,
            subtitle = membersLabel,
            positionMillis = positionMillis,
            durationMillis = song.durationMillis,
            isPlaying = isPlaying,
            onTogglePlay = onTogglePlay,
        )
    }
}

/** 무대 가운데 앉은 친구와, 그 둘레로 퍼지는 꽃가루 파티클. */
@Composable
private fun BoxScope.DancingCharacter(character: MascotCharacter, animation: MascotAnimation?, top: Dp, height: Dp) {
    val particles = remember { mutableStateListOf<Particle>() }
    var now by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) {
            withFrameMillis { frame ->
                now = frame - start
                particles.removeAll { !it.isAlive(now) }
            }
        }
    }
    LaunchedEffect(Unit) {
        val random = Random(System.nanoTime())
        while (true) {
            particles += randomParticle(random, bornAtMillis = now)
            delay(nextSpawnDelay(random))
        }
    }
    val note = painterResource(R.drawable.ic_music)
    Canvas(Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, (top + height / 2).toPx())
        particles.forEach { drawParticle(it, now, center, note) }
    }
    if (animation != null) {
        // 애니메이션 프레임은 정사각형이고 캐릭터가 높이의 약 93%를 차지한다. 멈춘 이미지와 같은 크기로 보이게 맞춘다
        AnimatedMascot(
            animation = animation,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = top - height * ANIMATION_LIFT_RATIO)
                .size(height * ANIMATION_SIZE_RATIO),
        )
    } else {
        Mascot(
            character = character,
            pose = MascotPose.Sitting,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = top)
                .height(height),
        )
    }
}

/**
 * S06 기본 보기. 왼쪽 무대에서 친구가 춤추고, 오른쪽에서 제목·가사·재생 막대를 보여 준다.
 * 가사 줄을 누르면 그 부분부터 듣는다.
 */
@Composable
private fun BasicView(
    song: CompletedSong,
    character: MascotCharacter,
    isPlaying: Boolean,
    positionMillis: Long,
    saving: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onEnlarge: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onMakeAnother: () -> Unit,
) {
    SubPage(title = "완성!", onBack = onBack) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(StageShape)
                    .background(AppColors.CompletedStageBackground),
            ) {
                StageFloor()
                DancingCharacter(
                    character,
                    animation = mascotAnimationFor(character, speedZoneForBpm(song.bpm)),
                    top = BasicCharacterTop,
                    height = BasicCharacterHeight,
                )
                SpeechBubble(
                    text = "노래가 완성되었어!",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 166.dp),
                )
                StageViewButton(
                    text = "크게 보기",
                    icon = R.drawable.ic_expand,
                    onClick = onEnlarge,
                    modifier = Modifier.offset(x = 39.dp, y = 36.dp),
                )
                HintChip(
                    text = "${character.nickname.josa("과", "와")} 함께 우리 노래에 맞춰 춤을 춰요",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 840.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
                    SongTitle(song.title)
                    LyricsPanel(song, positionMillis, onSeek)
                    BasicPlayer(isPlaying, positionMillis, song.durationMillis, onTogglePlay, onSeek)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    LargeButton(
                        text = if (saving) "저장하는 중" else "보관함에 저장",
                        onClick = onSave,
                        enabled = !saving,
                        icon = R.drawable.ic_save,
                        modifier = Modifier.weight(1f),
                    )
                    LargeButton(
                        text = "한 곡 더",
                        onClick = onMakeAnother,
                        icon = R.drawable.ic_replay,
                        variant = LargeButtonVariant.Secondary,
                        // 시안 너비 242에서는 좌우 여백 40을 빼면 글자가 잘려, 다 보이는 만큼만 넓힌다
                        modifier = Modifier.widthIn(min = 242.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SongTitle(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("오늘 만든 노래", style = AppTypography.LabelMedium, color = AppColors.HintChipText)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = SongTitleStyle,
                color = AppColors.TextDarkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            // TODO(디자인): 제목 바꾸기 팝업(S06-2) 시안이 오면 연결한다
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(EditButtonShape)
                    .background(AppColors.ButtonWarmSecondary)
                    .clickable(role = Role.Button, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_edit), contentDescription = "제목 바꾸기", tint = AppColors.TextDarkPrimary, modifier = Modifier.size(44.dp))
            }
        }
    }
}

/** 가사 전체. 지금 부르는 줄만 진하게, 단어는 주황으로 칠한다. 벌스·후렴이 바뀌는 곳은 한 줄 띄운다. */
@Composable
private fun LyricsPanel(song: CompletedSong, positionMillis: Long, onSeek: (Long) -> Unit) {
    val current = lyricIndexAt(song, positionMillis)
    val listState = rememberLazyListState()
    LaunchedEffect(current) {
        if (song.lyrics.isNotEmpty()) listState.animateScrollToItem((current - 1).coerceAtLeast(0))
    }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(LyricsPanelHeight)
            .clip(LyricsPanelShape)
            .background(AppColors.SurfaceBasic),
        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        itemsIndexed(song.lyrics) { index, line ->
            val section = song.lyricSections.getOrNull(index)
            if (index > 0 && section != null && section != song.lyricSections.getOrNull(index - 1)) Spacer(Modifier.height(30.dp))
            val active = index == current
            Text(
                text = if (active) highlighted(line, song.highlightWord) else AnnotatedString(line),
                style = if (active) AppTypography.TitleLargeExtraStrong else AppTypography.TitleLargeNormal,
                color = if (active) AppColors.TextDarkPrimary else AppColors.RecordingTime,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        song.lyricStartsMillis.getOrNull(index)?.let { start ->
                            Modifier.clickable(role = Role.Button) { onSeek(start) }
                        } ?: Modifier,
                    ),
            )
        }
    }
}

private fun highlighted(line: String, word: String?): AnnotatedString = buildAnnotatedString {
    append(line)
    if (!word.isNullOrBlank()) {
        var from = line.indexOf(word)
        while (from >= 0) {
            addStyle(SpanStyle(color = AppColors.BrandOrange), from, from + word.length)
            from = line.indexOf(word, from + word.length)
        }
    }
}

@Composable
private fun BasicPlayer(isPlaying: Boolean, positionMillis: Long, durationMillis: Long, onTogglePlay: () -> Unit, onSeek: (Long) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AppColors.BasicPlayButton)
                .clickable(role = Role.Button, onClick = onTogglePlay),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = if (isPlaying) "멈추기" else "재생",
                tint = AppColors.BasicStaticLight100,
                modifier = Modifier.size(52.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val fraction = if (durationMillis > 0) (positionMillis.toFloat() / durationMillis).coerceIn(0f, 1f) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(ProgressShape)
                    .background(AppColors.BasicProgressTrack)
                    .pointerInput(durationMillis) {
                        detectTapGestures { offset -> if (durationMillis > 0) onSeek((offset.x / size.width * durationMillis).toLong()) }
                    },
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(ProgressShape)
                        .background(AppColors.BrandOrange),
                )
            }
            Text(
                "${formatPlayTime(positionMillis)} / ${formatPlayTime(durationMillis)}",
                style = AppTypography.LabelMedium,
                color = AppColors.RecordingTime,
            )
        }
    }
}

private fun DrawScope.drawParticle(particle: Particle, nowMillis: Long, center: Offset, note: Painter) {
    val t = particle.progressAt(nowMillis)
    // 처음엔 빠르게 퍼지다가 천천히 멈춘다
    val eased = 1f - (1f - t) * (1f - t)
    val radians = Math.toRadians(particle.angleDegrees.toDouble())
    val distance = ParticleStartRadius.toPx() + particle.distanceDp.dp.toPx() * eased
    val position = center + Offset((cos(radians) * distance).toFloat(), (sin(radians) * distance).toFloat())
    val side = particle.sizeDp.dp.toPx()
    val alpha = 1f - t
    translate(position.x, position.y) {
        rotate(particle.rotationDegrees * eased, pivot = Offset.Zero) {
            when (particle.shape) {
                ParticleShape.OrangeNote, ParticleShape.YellowNote -> {
                    val color = if (particle.shape == ParticleShape.OrangeNote) AppColors.ParticleOrange else AppColors.ParticleYellow
                    translate(-side, -side) {
                        with(note) { draw(Size(side * 2, side * 2), alpha = alpha, colorFilter = ColorFilter.tint(color)) }
                    }
                }
                ParticleShape.OrangeDot -> drawCircle(AppColors.ParticleOrange, radius = side / 2, center = Offset.Zero, alpha = alpha)
                ParticleShape.OrangeTile -> drawRoundRect(
                    color = AppColors.ParticleOrange,
                    topLeft = Offset(-side / 2, -side / 2),
                    size = Size(side, side),
                    cornerRadius = CornerRadius(side / 3),
                    alpha = alpha,
                )
                ParticleShape.YellowTile -> drawRoundRect(
                    color = AppColors.ParticleYellow,
                    topLeft = Offset(-side * 0.8f, -side / 2),
                    size = Size(side * 1.6f, side),
                    cornerRadius = CornerRadius(side / 2),
                    alpha = alpha,
                )
            }
        }
    }
}

@Composable
private fun StageViewButton(text: String, icon: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .width(258.dp)
            .height(87.dp)
            .clip(PillShape)
            .background(AppColors.ButtonWarmSecondary)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = AppColors.TextWarmStrong, modifier = Modifier.size(52.dp))
        Text(text, style = ShrinkLabelStyle, color = AppColors.TextWarmStrong)
    }
}

/** 지금 부르는 가사 한 줄. 제목 단어를 주황으로 강조한다. */
@Composable
private fun LyricLine(line: String, highlight: String?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.ic_music), contentDescription = null, tint = AppColors.BrandOrange, modifier = Modifier.size(36.dp))
        val text = buildAnnotatedString {
            val start = highlight?.let { line.indexOf(it) } ?: -1
            if (highlight == null || start < 0) {
                append(line)
            } else {
                append(line.substring(0, start))
                withStyle(SpanStyle(color = AppColors.BrandOrange)) { append(highlight) }
                append(line.substring(start + highlight.length))
            }
        }
        Text(text, style = AppTypography.TitleSmall, color = AppColors.TextDarkPrimary, maxLines = 1)
    }
}

@Composable
private fun MiniPlayer(
    title: String,
    subtitle: String,
    positionMillis: Long,
    durationMillis: Long,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(182.dp)
            .dropShadow(Color.Black.copy(alpha = 0.08f), 6.dp, 24.dp, 32.dp)
            .clip(PlayerShape)
            .background(AppColors.MiniPlayerBackground)
            .border(PLAYER_BORDER, AppColors.PopupWarmBorder, PlayerShape)
            .padding(horizontal = 41.dp),
        horizontalArrangement = Arrangement.spacedBy(45.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = AppTypography.BodyLargeStrong, color = AppColors.TextDarkPrimary, maxLines = 1)
                Text(subtitle, style = PlayerMetaStyle, color = AppColors.TextDarkTertiary, maxLines = 1)
            }
            val fraction = if (durationMillis > 0) positionMillis.toFloat() / durationMillis else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(TrackShape)
                    .background(AppColors.MiniPlayerTrack),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .height(12.dp)
                        .clip(TrackShape)
                        .background(AppColors.BrandOrange),
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(formatPlayTime(positionMillis), style = PlayerMetaStyle, color = AppColors.TextDarkTertiary)
                Spacer(Modifier.weight(1f))
                Text(formatPlayTime(durationMillis), style = PlayerMetaStyle, color = AppColors.TextDarkTertiary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(31.dp)) {
            PlayerButton(
                icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                contentDescription = if (isPlaying) "일시정지" else "재생",
                onClick = onTogglePlay,
            )
            // TODO(오디오): 보관함의 다음 곡으로 넘긴다
            PlayerButton(icon = R.drawable.ic_next, contentDescription = "다음 곡", onClick = {})
        }
    }
}

@Composable
private fun PlayerButton(icon: Int, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(103.dp)
            .clip(CircleShape)
            .background(AppColors.ButtonWarmSecondary)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = contentDescription, tint = AppColors.TextWarmStrong, modifier = Modifier.size(67.dp))
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun CompletedScreenPreview() {
    CompletedScreen(
        song = CompletedSong("사과 노래", "사과", listOf("사과는 빨갛다, 둥둥 둥둥"), 102_000, 0, bpm = 110),
        character = MascotCharacter.LION,
        membersLabel = "엄마 북 · 첫째 탬버린 · 둘째 셰이커",
        isPlaying = true,
        positionMillis = 0,
        saving = false,
        onTogglePlay = {},
        onBack = {},
        onChangeCharacter = {},
        onSave = {},
        onMakeAnother = {},
    )
}
