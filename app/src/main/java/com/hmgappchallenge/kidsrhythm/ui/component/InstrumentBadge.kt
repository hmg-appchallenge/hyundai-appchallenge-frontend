package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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

/**
 * 악기 분류. 고르기 화면의 분류 칩과 같은 순서다.
 * TODO(기획): 악기 12종 목록과 분류가 오면 맞춘다
 */
enum class InstrumentCategory(val label: String) {
    Percussion("두드리는 악기"),
    Shaken("흔드는 악기"),
    String("줄 악기"),
    Keys("건반 · 종"),
}

/**
 * 가족 목소리마다 고르는 악기.
 * [iconRatio]는 원형 배지 지름 대비 아이콘 크기다. 처음 3종과 나중에 추가된 악기의 비율이 다르다.
 * [pictogramRes]가 없는 악기는 아이콘을 어둡게 칠해 픽토그램 자리에 쓴다.
 */
enum class Instrument(
    val label: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val pictogramRes: Int?,
    val background: Color,
    val category: InstrumentCategory,
    val iconRatio: Float = ORIGINAL_ICON_RATIO,
) {
    DRUM("북", R.drawable.ic_instrument_drum, R.drawable.img_instrument_drum, AppColors.DrumBackground, InstrumentCategory.Percussion),
    TAMBOURINE("탬버린", R.drawable.ic_instrument_tambourine, R.drawable.img_instrument_tambourine, AppColors.TambourineBackground, InstrumentCategory.Shaken),
    SHAKER("셰이커", R.drawable.ic_instrument_shaker, R.drawable.img_instrument_shaker, AppColors.ShakerBackground, InstrumentCategory.Shaken),
    BELL("벨", R.drawable.ic_instrument_bell, null, AppColors.BellBackground, InstrumentCategory.Keys, ADDED_ICON_RATIO),
    UKULELE("우쿨렐레", R.drawable.ic_instrument_ukulele, null, AppColors.UkuleleBackground, InstrumentCategory.String, ADDED_ICON_RATIO),
    STEELDRUM("스틸드럼", R.drawable.ic_instrument_steeldrum, null, AppColors.SteeldrumBackground, InstrumentCategory.Percussion, ADDED_ICON_RATIO),
    GUITAR("기타", R.drawable.ic_instrument_guitar, null, AppColors.GuitarBackground, InstrumentCategory.String, ADDED_ICON_RATIO),

    // TODO(디자인): 실로폰 픽토그램이 오면 바꾼다. 시안도 음표 아이콘을 임시로 쓴다
    XYLOPHONE("실로폰", R.drawable.ic_music, null, AppColors.XylophoneBackground, InstrumentCategory.Keys, ADDED_ICON_RATIO),
}

private const val ORIGINAL_ICON_RATIO = 64f / 120f
private const val ADDED_ICON_RATIO = 44f / 72f

/**
 * 악기 원형 배지.
 * 목소리 카드의 아바타처럼 원만 작아지는 경우가 있어 아이콘 크기를 따로 받을 수 있다.
 */
@Composable
fun InstrumentBadge(
    instrument: Instrument,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    iconSize: Dp = size * instrument.iconRatio,
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
    val pictogram = instrument.pictogramRes
    if (pictogram != null) {
        Image(painter = painterResource(pictogram), contentDescription = instrument.label, modifier = modifier.size(size))
    } else {
        Icon(
            painter = painterResource(instrument.iconRes),
            contentDescription = instrument.label,
            tint = AppColors.IconDarkPrimary,
            modifier = modifier.size(size),
        )
    }
}

@Preview
@Composable
private fun InstrumentBadgePreview() {
    InstrumentBadge(Instrument.SHAKER)
}
