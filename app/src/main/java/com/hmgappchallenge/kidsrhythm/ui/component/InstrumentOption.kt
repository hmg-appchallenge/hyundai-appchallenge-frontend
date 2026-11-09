package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val CardShape = RoundedCornerShape(32.dp)
private val BORDER_WIDTH = 2.dp

/**
 * 악기 세트 선택 카드.
 * 누르면 3초 미리듣기를 하는 용도라 [onClick]을 받는다. 선택 상태는 아직 정해지지 않았다.
 */
@Composable
fun InstrumentOption(
    instrument: Instrument,
    name: String,
    season: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Column(
        // 기본 크기는 258×300. 호출하는 쪽이 modifier로 크기를 정하면 그 크기를 따른다 (먼저 적용한 크기가 이긴다)
        modifier = modifier
            .size(width = 258.dp, height = 300.dp)
            .clip(CardShape)
            .background(AppColors.SurfaceBasic)
            .border(BORDER_WIDTH, AppColors.BasicAlphaLight100, CardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            // border는 공간을 차지하지 않으므로 패딩에 두께를 더한다
            .padding(start = 16.dp + BORDER_WIDTH, end = 16.dp + BORDER_WIDTH, top = 32.dp + BORDER_WIDTH, bottom = 28.dp + BORDER_WIDTH),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        InstrumentBadge(instrument)
        Text(text = name, style = AppTypography.TitleSmall, color = AppColors.TextDarkPrimary, maxLines = 1)
        Text(text = season, style = AppTypography.LabelMedium, color = AppColors.TextDarkTertiary, maxLines = 1)
    }
}

@Preview
@Composable
private fun InstrumentOptionPreview() {
    InstrumentOption(Instrument.DRUM, name = "Name", season = "Season", onClick = {})
}
