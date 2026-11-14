package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.beat.BeatPosition
import com.hmgappchallenge.kidsrhythm.beat.speedZoneForBpm
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val StageShape = RoundedCornerShape(48.dp)
private val LyricBarShape = RoundedCornerShape(32.dp)

/** 크게 보기 무대 (가로 전체). */
private val BigStageHeight = 479.dp
private val BigFloorTop = 334.dp
private val BigCharacterTop = 67.dp
private val BigCharacterHeight = 363.dp

/** 가사를 펼쳤을 때 가운데로 줄어든 무대(12칸 중 가운데 8칸)와 친구, 가사 상자 높이. */
private const val FOCUS_STAGE_WIDTH = 1022f / 1572f
private val FocusStageHeight = 404.dp
private val FocusFloorTop = 312.dp
private val FocusCharacterTop = 43.dp
private val FocusCharacterHeight = 340.dp
private val FocusLyricsHeight = 236.dp

/**
 * 노래를 들을 때의 크게 보기 (S06 완성 · S07-1 다시 듣기 정차 중 공통).
 * 가로로 넓은 무대 아래에 주황 가사 막대([lyric])와 [player], 오른쪽 아래 [actions]를 둔다.
 * 가사 막대를 누르면 [onOpenLyrics], 무대의 작게 보기를 누르면 [onShrink].
 */
@Composable
fun ColumnScope.BigStageLayout(
    character: MascotCharacter,
    bpm: Int,
    beat: BeatPosition?,
    lyric: AnnotatedString,
    onShrink: () -> Unit,
    onOpenLyrics: () -> Unit,
    player: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    stageOverlay: @Composable BoxScope.() -> Unit = {},
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
            animation = mascotAnimationFor(character, speedZoneForBpm(bpm)),
            top = BigCharacterTop,
            height = BigCharacterHeight,
            beat = beat,
        )
        stageOverlay()
        StageViewButton(text = "작게 보기", icon = R.drawable.ic_shrink, onClick = onShrink, modifier = Modifier.offset(x = 39.dp, y = 36.dp))
    }
    LyricBar(
        lyric,
        onClick = onOpenLyrics,
        modifier = Modifier
            .padding(top = 31.dp)
            .fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    player()
    Spacer(Modifier.weight(1f))
    StageActions(actions)
}

/**
 * 가사를 펼친 모습. 무대는 가운데로 줄고, 아래에 지난·지금·다음 가사 줄, [player], 오른쪽 아래 [actions]를 둔다.
 * 가사 줄을 누르면 [onLineClick], ⌄나 무대의 크게 보기를 누르면 [onFold]로 다시 접는다.
 */
@Composable
fun ColumnScope.LyricsFocusLayout(
    character: MascotCharacter,
    bpm: Int,
    beat: BeatPosition?,
    lines: List<String>,
    current: Int,
    activeText: (Int) -> AnnotatedString,
    onLineClick: (Int) -> Unit,
    onFold: () -> Unit,
    player: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    stageOverlay: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(FOCUS_STAGE_WIDTH)
            .height(FocusStageHeight)
            .clip(StageShape)
            .background(AppColors.CompletedStageBackground),
    ) {
        StageFloor(top = FocusFloorTop)
        DancingCharacter(
            character,
            animation = mascotAnimationFor(character, speedZoneForBpm(bpm)),
            top = FocusCharacterTop,
            height = FocusCharacterHeight,
            beat = beat,
        )
        stageOverlay()
        StageViewButton(text = "크게 보기", icon = R.drawable.ic_expand, onClick = onFold, modifier = Modifier.offset(x = 39.dp, y = 36.dp))
    }
    LyricsPeek(
        lines = lines,
        current = current,
        activeText = activeText,
        onLineClick = onLineClick,
        nextLines = 1,
        fade = false,
        contentPadding = PaddingValues(horizontal = 24.dp),
        lineSpacing = 11.dp,
        modifier = Modifier
            .padding(top = 27.dp)
            .fillMaxWidth()
            .height(FocusLyricsHeight),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 22.dp, end = 34.dp)
                .size(52.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onFold),
            contentAlignment = Alignment.Center,
        ) {
            // 뒤로 가기 화살표를 아래로 돌려 접기 표시로 쓴다
            Icon(
                painterResource(R.drawable.ic_back),
                contentDescription = "가사 접기",
                tint = AppColors.LyricsFoldIcon,
                modifier = Modifier
                    .size(52.dp)
                    .rotate(-90f),
            )
        }
    }
    Spacer(Modifier.height(24.dp))
    player()
    Spacer(Modifier.weight(1f))
    StageActions(actions)
}

/** 오른쪽 아래에 붙는 버튼 줄. */
@Composable
private fun StageActions(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.End),
        content = content,
    )
}

/** 주황 가사 막대: 지금 부르는 한 줄([text], 강조 글자는 [AppColors.LyricBarHighlight]로 칠해 넘긴다)과 펼치기 표시. */
@Composable
fun LyricBar(text: AnnotatedString, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(100.dp)
            .clip(LyricBarShape)
            .background(AppColors.BrandOrange)
            .clickable(role = Role.Button, onClickLabel = "가사 펼치기", onClick = onClick)
            .padding(start = 43.dp, end = 34.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_music), contentDescription = null, tint = AppColors.LyricBarHighlight, modifier = Modifier.size(36.dp))
        Text(text, style = AppTypography.TitleSmall, color = AppColors.LyricBarText, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        // 뒤로 가기 화살표를 위로 돌려 펼치기 표시로 쓴다
        Icon(
            painterResource(R.drawable.ic_back),
            contentDescription = null,
            tint = AppColors.LyricBarHighlight,
            modifier = Modifier
                .size(52.dp)
                .rotate(90f),
        )
    }
}
