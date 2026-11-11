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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors

/** 악기 분류. 고르기 화면의 분류 칩과 같은 순서다. */
enum class InstrumentCategory(val label: String) {
    Percussion("두드리는 악기"),
    Shaken("흔드는 악기"),
    String("줄 악기"),
    Keys("건반 · 종"),
}

/** 원형 배지(지름 [BADGE_BASE_SIZE]) 안에 그리는 아이콘 크기. 악기마다 모양이 달라 크기도 다르다. */
private val IconLarge = DpSize(64.dp, 64.dp)

/**
 * 가족 목소리마다 고르는 악기 12종. 악기 고르기 '전체'에 시안 순서대로 나온다.
 * [iconSize]는 지름 [BADGE_BASE_SIZE] 배지 기준 아이콘 크기다.
 */
enum class Instrument(
    val label: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val pictogramRes: Int,
    val background: Color,
    val category: InstrumentCategory,
    val iconSize: DpSize = IconLarge,
) {
    DRUM("북", R.drawable.ic_instrument_drum, R.drawable.img_instrument_drum, AppColors.DrumBackground, InstrumentCategory.Percussion),
    TAMBOURINE("탬버린", R.drawable.ic_instrument_tambourine, R.drawable.img_instrument_tambourine, AppColors.TambourineBackground, InstrumentCategory.Shaken),
    SHAKER("셰이커", R.drawable.ic_instrument_shaker, R.drawable.img_instrument_shaker, AppColors.ShakerBackground, InstrumentCategory.Shaken),
    BELL(
        "벨", R.drawable.ic_instrument_bell, R.drawable.img_instrument_bell, AppColors.BellBackground,
        InstrumentCategory.Keys, DpSize(54.dp, 62.dp),
    ),
    UKULELE(
        "우쿨렐레", R.drawable.ic_instrument_ukulele, R.drawable.img_instrument_ukulele, AppColors.UkuleleBackground,
        InstrumentCategory.String, DpSize(50.dp, 50.dp),
    ),
    STEELDRUM(
        "스틸드럼", R.drawable.ic_instrument_steeldrum, R.drawable.img_instrument_steeldrum, AppColors.SteeldrumBackground,
        InstrumentCategory.Percussion, DpSize(48.dp, 48.dp),
    ),
    WOODBLOCK(
        "우드블록", R.drawable.ic_instrument_woodblock, R.drawable.img_instrument_woodblock, AppColors.WoodblockBackground,
        InstrumentCategory.Percussion, DpSize(52.dp, 52.dp),
    ),
    GUITAR(
        "기타", R.drawable.ic_instrument_guitar, R.drawable.img_instrument_guitar, AppColors.GuitarBackground,
        InstrumentCategory.String, DpSize(56.dp, 56.dp),
    ),
    HARP("하프", R.drawable.ic_instrument_harp, R.drawable.img_instrument_harp, AppColors.HarpBackground, InstrumentCategory.String),
    XYLOPHONE("실로폰", R.drawable.ic_instrument_xylophone, R.drawable.img_instrument_xylophone, AppColors.XylophoneBackground, InstrumentCategory.Keys),
    PIANO("피아노", R.drawable.ic_instrument_piano, R.drawable.img_instrument_piano, AppColors.PianoBackground, InstrumentCategory.Keys),
    MARACAS("마라카스", R.drawable.ic_instrument_maracas, R.drawable.img_instrument_maracas, AppColors.MaracasBackground, InstrumentCategory.Shaken),
}

/** [Instrument.iconSize]의 기준이 되는 배지 지름. */
val BADGE_BASE_SIZE = 120.dp

/**
 * 악기 원형 배지.
 * 목소리 카드의 아바타처럼 원만 작아지는 경우가 있어 아이콘 크기를 따로 받을 수 있다.
 */
@Composable
fun InstrumentBadge(
    instrument: Instrument,
    modifier: Modifier = Modifier,
    size: Dp = BADGE_BASE_SIZE,
    iconSize: DpSize = instrument.iconSize * (size / BADGE_BASE_SIZE),
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
    Image(painter = painterResource(instrument.pictogramRes), contentDescription = instrument.label, modifier = modifier.size(size))
}

@Preview
@Composable
private fun InstrumentBadgePreview() {
    InstrumentBadge(Instrument.SHAKER)
}
