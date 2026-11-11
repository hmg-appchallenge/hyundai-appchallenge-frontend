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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
fun StageFloor(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val top = StageFloorTop.toPx()
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

/** 홈(S01)과 친구 고르기(S01-1) 스테이지 가운데에 서서 손을 흔드는 친구. */
@Composable
fun BoxScope.StandingCharacter(character: MascotCharacter, modifier: Modifier = Modifier) {
    Mascot(
        character = character,
        pose = MascotPose.Standing,
        modifier = modifier
            .align(Alignment.TopCenter)
            .padding(top = StandingCharacterTop)
            .height(StandingCharacterHeight),
    )
}
