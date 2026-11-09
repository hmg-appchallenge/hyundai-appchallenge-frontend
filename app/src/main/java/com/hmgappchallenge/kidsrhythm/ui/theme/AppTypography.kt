package com.hmgappchallenge.kidsrhythm.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hmgappchallenge.kidsrhythm.R

@OptIn(ExperimentalTextApi::class)
private fun astaSans(weight: FontWeight) = Font(
    resId = R.font.asta_sans,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Asta Sans 가변 폰트(wght 300–800). 라이선스는 assets/licenses/AstaSans-OFL.txt. */
val AstaSans = FontFamily(
    astaSans(FontWeight.Normal),
    astaSans(FontWeight.Bold),
    astaSans(FontWeight.ExtraBold),
)

private fun style(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
    fontFamily = AstaSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
)

/** 텍스트 스타일 토큰. */
object AppTypography {
    val HeadlineLarge = style(60, 80, FontWeight.ExtraBold)

    val TitleLarge = style(40, 52, FontWeight.Bold)
    val TitleLargeExtraStrong = style(40, 52, FontWeight.ExtraBold)
    val TitleLargeNormal = style(40, 52, FontWeight.Normal)
    val TitleMedium = style(36, 44, FontWeight.Bold)
    val TitleMediumNormal = style(36, 44, FontWeight.Normal)
    val TitleSmall = style(32, 40, FontWeight.Bold)
    val TitleSmallExtraStrong = style(32, 40, FontWeight.ExtraBold)
    val TitleSmallNormal = style(32, 40, FontWeight.Normal)

    val BodyLargeStrong = style(30, 38, FontWeight.Bold)
    val BodyMedium = style(28, 36, FontWeight.Normal)
    val BodySmallStrong = style(26, 34, FontWeight.Bold)

    val LabelMedium = style(24, 28, FontWeight.Normal)
    val LabelSmall = style(20, 24, FontWeight.Normal)

    /** 비트 화면의 큰 숫자 (`Pleos Connect/비트음량2`). */
    val BeatVolume = style(150, 186, FontWeight.Bold)
}
