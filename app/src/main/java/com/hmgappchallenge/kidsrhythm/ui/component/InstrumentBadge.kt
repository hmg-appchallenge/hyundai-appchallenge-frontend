package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors

/** 가족 목소리마다 고르는 악기. */
enum class Instrument(
    val label: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val pictogramRes: Int,
    val background: Color,
) {
    DRUM("북", R.drawable.ic_instrument_drum, R.drawable.img_instrument_drum, AppColors.DrumBackground),
    SHAKER("셰이커", R.drawable.ic_instrument_shaker, R.drawable.img_instrument_shaker, AppColors.ShakerBackground),
    TAMBOURINE("탬버린", R.drawable.ic_instrument_tambourine, R.drawable.img_instrument_tambourine, AppColors.TambourineBackground),
}

/**
 * 악기 원형 배지.
 * 목소리 카드의 아바타처럼 원만 작아지는 경우가 있어 아이콘 크기는 따로 받는다.
 */
@Composable
fun InstrumentBadge(
    instrument: Instrument,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    iconSize: Dp = 64.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(instrument.background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(instrument.iconRes),
            contentDescription = instrument.label,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** 악기 큰 픽토그램. */
@Composable
fun InstrumentPictogram(
    instrument: Instrument,
    modifier: Modifier = Modifier,
    size: Dp = 405.dp,
) {
    Image(
        painter = painterResource(instrument.pictogramRes),
        contentDescription = instrument.label,
        modifier = modifier.size(size),
    )
}

@Preview
@Composable
private fun InstrumentBadgePreview() {
    InstrumentBadge(Instrument.SHAKER)
}
