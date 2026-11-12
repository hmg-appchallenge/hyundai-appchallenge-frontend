package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val RowShape = RoundedCornerShape(32.dp)
private val BORDER_WIDTH = 2.dp

/** 곡 썸네일 색. 목록에서 곡마다 차례로 돌아가며 쓴다. */
enum class SongTone(val background: Color, val icon: Color) {
    Yellow(AppColors.TambourineBackground, AppColors.SongToneYellow),
    Blue(AppColors.ShakerBackground, AppColors.SongToneBlue),
    Green(AppColors.UkuleleBackground, AppColors.SongToneGreen),
    Purple(AppColors.XylophoneBackground, AppColors.SongTonePurple),
}

/** 목록 [index]번째 곡의 썸네일 색. */
fun songTone(index: Int): SongTone = SongTone.entries[index.mod(SongTone.entries.size)]

/** 곡 한 줄 (음표 썸네일 · 제목 · 부가 정보 · 재생 버튼). */
@Composable
fun SongRow(
    title: String,
    meta: String,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
    tone: SongTone = SongTone.Yellow,
) {
    Row(
        modifier = modifier
            .height(120.dp)
            .clip(RowShape)
            .background(AppColors.SurfaceBasic)
            .border(BORDER_WIDTH, AppColors.SpeedChipBackground, RowShape)
            // border는 공간을 차지하지 않으므로 패딩에 두께를 더한다
            .padding(start = 24.dp + BORDER_WIDTH, end = 16.dp + BORDER_WIDTH),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongToneTile(tone)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = AppTypography.BodyLargeStrong, color = AppColors.TextDarkPrimary, maxLines = 1)
            Text(text = meta, style = AppTypography.LabelMedium, color = AppColors.SongMeta, maxLines = 1)
        }
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppColors.SpeedChipBackground)
                .clickable(role = Role.Button, onClick = onPlay),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_play), contentDescription = "재생", tint = AppColors.TextDarkPrimary, modifier = Modifier.size(52.dp))
        }
    }
}

@Preview
@Composable
private fun SongRowPreview() {
    SongRow(title = "사과 노래", meta = "어제 · 1분 42초", onPlay = {})
}
