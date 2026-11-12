package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.completed.Particle
import com.hmgappchallenge.kidsrhythm.completed.ParticleShape
import com.hmgappchallenge.kidsrhythm.completed.nextSpawnDelay
import com.hmgappchallenge.kidsrhythm.completed.randomParticle
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * 애니메이션 프레임(512px, 캐릭터가 위 15px ~ 아래 492px)을 멈춘 이미지와 같은 높이·바닥에 맞춘다.
 * 멈춘 이미지 높이 430일 때 프레임 462, 위치는 14 위로 올린다.
 */
private const val ANIMATION_SIZE_RATIO = 462f / 430f
private const val ANIMATION_LIFT_RATIO = 14f / 430f

/** 파티클이 생기는 거리. 캐릭터 가운데에서 바로 생기면 몸에 가려 보이지 않아 가장자리쯤에서 시작한다. */
private val ParticleStartRadius = 200.dp

private val StageButtonShape = RoundedCornerShape(32.dp)
private val StageButtonLabelStyle = AppTypography.TitleMedium.copy(fontWeight = FontWeight.ExtraBold)

/** 무대 가운데 앉은 친구와, 그 둘레로 퍼지는 꽃가루 파티클. */
@Composable
fun BoxScope.DancingCharacter(character: MascotCharacter, animation: MascotAnimation?, top: Dp, height: Dp) {
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

/** 무대 왼쪽 위 '크게 보기' · '작게 보기' 버튼. */
@Composable
fun StageViewButton(text: String, icon: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .width(258.dp)
            .height(87.dp)
            .clip(StageButtonShape)
            .background(AppColors.ButtonWarmSecondary)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = AppColors.TextWarmStrong, modifier = Modifier.size(52.dp))
        Text(text, style = StageButtonLabelStyle, color = AppColors.TextWarmStrong)
    }
}
