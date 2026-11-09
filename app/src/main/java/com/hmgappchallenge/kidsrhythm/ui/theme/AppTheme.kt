package com.hmgappchallenge.kidsrhythm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    primary = AppColors.BrandOrange,
    onPrimary = AppColors.BasicStaticLight100,
    background = AppColors.AppBackground,
    onBackground = AppColors.TextDarkPrimary,
    surface = AppColors.AppBackground,
    onSurface = AppColors.TextDarkPrimary,
    onSurfaceVariant = AppColors.TextDarkTertiary,
)

private val Typography = Typography(
    headlineLarge = AppTypography.HeadlineLarge,
    titleLarge = AppTypography.TitleLarge,
    titleMedium = AppTypography.TitleMedium,
    titleSmall = AppTypography.TitleSmall,
    bodyLarge = AppTypography.BodyMedium,
    bodyMedium = AppTypography.BodyMedium,
    labelLarge = AppTypography.LabelMedium,
    labelMedium = AppTypography.LabelMedium,
    labelSmall = AppTypography.LabelSmall,
)

/** 화면에서 직접 쓰는 토큰은 [AppColors]·[AppTypography]에 있다. Material 컴포넌트가 쓰는 기본값만 여기서 연결한다. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, typography = Typography, content = content)
}
