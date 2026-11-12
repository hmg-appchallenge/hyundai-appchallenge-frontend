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

private val StageShape = RoundedCornerShape(48.dp)
private val PillShape = RoundedCornerShape(32.dp)
private val TrackShape = RoundedCornerShape(6.dp)
private val PlayerShape = RoundedCornerShape(32.dp)
private val PLAYER_BORDER = 2.dp

private val StageHeight = 598.dp
private val FloorTop = 480.dp

/** 앉은 친구가 무대 가운데에 앉는 위치와 높이. 파티클은 이 친구의 가운데에서 퍼진다. */
private val CharacterTop = 103.dp
private val CharacterHeight = 430.dp

/** 애니메이션 프레임(512px, 캐릭터가 위 15px ~ 아래 492px)을 멈춘 이미지와 같은 높이·바닥에 맞춘 크기와 위치. */
private val AnimationSize = 462.dp
private val AnimationTop = 89.dp

/** 파티클이 생기는 거리. 캐릭터 가운데에서 바로 생기면 몸에 가려 보이지 않아 가장자리쯤에서 시작한다. */
private val ParticleStartRadius = 200.dp

/** 공용 타이포 토큰에 없는 굵기·크기라 여기서만 쓴다. */
private val ShrinkLabelStyle = AppTypography.TitleMedium.copy(fontWeight = FontWeight.ExtraBold)
private val PlayerMetaStyle = AppTypography.LabelMedium.copy(fontSize = 22.sp)

/**
 * S06 노래 완성. 고른 친구가 무대에서 춤추고, 아래에서 지금 가사 한 줄과 미니 플레이어를 보여 준다.
 * 소리 재생이 아직 없어서 재생 위치는 시간으로 흉내 낸다.
 */
@Composable
fun CompletedScreen(
    song: CompletedSong,
    character: MascotCharacter,
    membersLabel: String,
    onBack: () -> Unit,
    onChangeCharacter: () -> Unit,
    onSave: () -> Unit,
    onMakeAnother: () -> Unit,
) {
    // TODO(오디오): 받은 mp3를 실제로 재생하고 그 위치를 쓴다
    var isPlaying by remember { mutableStateOf(true) }
    var positionMillis by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var last = withFrameMillis { it }
        while (positionMillis < song.durationMillis) {
            withFrameMillis { now ->
                positionMillis = (positionMillis + (now - last)).coerceAtMost(song.durationMillis)
                last = now
            }
        }
        isPlaying = false
    }
    val lyric = song.lyrics.getOrNull(lyricIndexAt(positionMillis, song.durationMillis, song.lyrics.size)).orEmpty()

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
            DancingCharacter(character, animation = mascotAnimationFor(character, speedZoneForBpm(song.bpm)))
            // TODO(디자인): 기본 보기(작게 보기) 시안이 오면 연결한다
            ShrinkButton(onClick = {}, modifier = Modifier.offset(x = 39.dp, y = 36.dp))
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
            LargeButton(text = "보관함에 저장", onClick = onSave, icon = R.drawable.ic_save, modifier = Modifier.width(320.dp))
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
            onTogglePlay = {
                // 끝까지 들은 뒤 다시 누르면 처음부터 듣는다
                if (!isPlaying && positionMillis >= song.durationMillis) positionMillis = 0
                isPlaying = !isPlaying
            },
        )
    }
}

/** 무대 가운데 앉은 친구와, 그 둘레로 퍼지는 꽃가루 파티클. */
@Composable
private fun BoxScope.DancingCharacter(character: MascotCharacter, animation: MascotAnimation?) {
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
        val center = Offset(size.width / 2, (CharacterTop + CharacterHeight / 2).toPx())
        particles.forEach { drawParticle(it, now, center, note) }
    }
    if (animation != null) {
        // 애니메이션 프레임은 정사각형이고 캐릭터가 높이의 약 93%를 차지한다. 멈춘 이미지와 같은 크기로 보이게 맞춘다
        AnimatedMascot(
            animation = animation,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = AnimationTop)
                .size(AnimationSize),
        )
    } else {
        Mascot(
            character = character,
            pose = MascotPose.Sitting,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = CharacterTop)
                .height(CharacterHeight),
        )
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
private fun ShrinkButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
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
        Icon(painterResource(R.drawable.ic_shrink), contentDescription = null, tint = AppColors.TextWarmStrong, modifier = Modifier.size(52.dp))
        Text("작게 보기", style = ShrinkLabelStyle, color = AppColors.TextWarmStrong)
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
        onBack = {},
        onChangeCharacter = {},
        onSave = {},
        onMakeAnother = {},
    )
}
