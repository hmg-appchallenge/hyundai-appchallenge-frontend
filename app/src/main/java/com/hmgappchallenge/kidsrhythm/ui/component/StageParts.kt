package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val StageFloorTop = 701.dp
private val StageFloorFadeHeight = 185.dp

private val BubbleRadius = 32.dp
private val BubbleShape = RoundedCornerShape(BubbleRadius)

/** 캐릭터 위 말풍선. 꼬리는 아래 가운데에 붙는다. */
@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .dropShadow(AppColors.SpeechBubbleShadow, 6.dp, 24.dp, BubbleRadius)
                .clip(BubbleShape)
                .background(AppColors.StageBubbleBackground)
                .padding(horizontal = 36.dp, vertical = 22.dp),
        ) {
            Text(text = text, style = AppTypography.BodyLargeStrong, color = AppColors.TextDarkPrimary, textAlign = TextAlign.Center)
        }
        // 꼬리를 말풍선에 살짝 겹쳐 이음매를 숨긴다
        Image(
            painter = painterResource(R.drawable.img_speech_bubble_tail),
            contentDescription = null,
            modifier = Modifier
                .offset(y = (-1.5).dp)
                .rotate(180f),
        )
    }
}

/** 스테이지 바닥. 일정 높이부터 바닥색이 아래로 옅어진다. 스테이지 상자를 가득 채워 그린다. */
@Composable
fun StageFloor(modifier: Modifier = Modifier, top: Dp = StageFloorTop) {
    Canvas(modifier.fillMaxSize()) {
        val top = top.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(AppColors.StageFloor, Color.Transparent),
                startY = top,
                endY = top + StageFloorFadeHeight.toPx(),
            ),
            topLeft = Offset(0f, top),
            size = Size(size.width, size.height - top),
        )
    }
}

/** 스테이지 아래 안내 칩 (음표 아이콘 + 한 줄 문구). */
@Composable
fun HintChip(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(AppColors.StageBubbleBackground)
            .padding(start = 24.dp, end = 32.dp, top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_music),
            contentDescription = null,
            tint = AppColors.BrandOrange,
            modifier = Modifier.size(40.dp),
        )
        Text(text = text, style = HintTextStyle, color = AppColors.HintChipText, maxLines = 1)
    }
}

/** 안내 칩 글자 (24/28 Bold). 공용 타이포 토큰에 없는 굵기라 여기서만 쓴다. */
private val HintTextStyle = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Bold)

@Preview
@Composable
private fun StagePartsPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SpeechBubble("안녕! 우리 함께 노래를 만들어볼까?")
        HintChip("노래를 완성하면 친구가 나와서 춤춰요")
    }
}

/** 서 있는 친구가 스테이지 바닥에 서는 높이와 위쪽 위치. 여백 없는 이미지를 같은 높이로 맞춘다. */
private val StandingCharacterHeight = 442.dp
private val StandingCharacterTop = 287.dp

/** 살랑이는 한 번(왼쪽 → 오른쪽)에 걸리는 시간. */
private const val SWAY_MILLIS = 1_400

/** 살랑일 때 발끝을 축으로 기우는 최대 각도. */
private const val SWAY_DEGREES = 3f

/** 살랑일 때 위로 들썩이는 최대 높이. */
private val BobHeight = 8.dp

/**
 * 홈(S01)과 친구 고르기(S01-1) 스테이지 가운데에 서서 손을 흔드는 친구.
 * [swaying]이면 발끝을 축으로 좌우로 살랑이며 살짝 들썩인다 (홈 대기 화면).
 */
@Composable
fun BoxScope.StandingCharacter(character: MascotCharacter, modifier: Modifier = Modifier, swaying: Boolean = false) {
    val motion = if (swaying) {
        val transition = rememberInfiniteTransition(label = "sway")
        val phase by transition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(SWAY_MILLIS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "swayPhase",
        )
        Modifier.graphicsLayer {
            transformOrigin = TransformOrigin(0.5f, 1f)
            rotationZ = phase * SWAY_DEGREES
            // 가운데를 지날 때 가장 높이 뜨고 양 끝에서 내려앉는다
            translationY = -BobHeight.toPx() * (1f - phase * phase)
        }
    } else {
        Modifier
    }
    Mascot(
        character = character,
        pose = MascotPose.Standing,
        modifier = modifier
            .align(Alignment.TopCenter)
            .padding(top = StandingCharacterTop)
            .height(StandingCharacterHeight)
            .then(motion),
    )
}
