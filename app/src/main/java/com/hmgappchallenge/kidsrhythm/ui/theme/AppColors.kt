package com.hmgappchallenge.kidsrhythm.ui.theme

import androidx.compose.ui.graphics.Color

/** 색상 토큰. */
object AppColors {
    // Text
    // 메인 블랙 (제목·주요 글자)
    val TextDarkPrimary = AppPalette.WarmBlack1000
    val TextDarkSecondary = Color(0xD6131417)
    val TextDarkTertiary = Color(0xA3131417)
    val TextDarkQuaternary = Color(0x52131417)

    // Surface · Background
    val SurfaceBasic = Color(0xFFFFFFFF)
    val BackgroundPrimary = Color(0xFFF7F8FA)
    val BackgroundSecondary = Color(0xFFFFFFFF)
    val BackgroundPopup = Color(0xFFFFFFFF)

    // Basic
    val BasicAlphaLight50 = Color(0x0D131417)
    val BasicAlphaLight100 = Color(0x1A131417)
    val BasicAlphaLight200 = Color(0x33131417)
    val BasicGrayscale100 = Color(0xFFEDEEF2)
    val BasicStaticLight100 = Color(0xFFFFFFFF)

    // Icon
    val IconDarkPrimary = AppPalette.WarmBlack1000
    val IconDarkSecondary = Color(0xD6131417)
    val IconDarkQuaternary = Color(0x52131417)

    // Button · Fields
    val ButtonFilledEnabled = Color(0xFF44464E)
    val ButtonBasicDisabled = Color(0x33131417)
    val FieldBackground = Color(0xFFFFFFFF)
    val FieldFocused = Color(0xFF313236)

    // Media · Informative
    val MusicPrimary = Color(0xFF4781FF)
    val InformativePositive = Color(0xFF0064FF)
    val InformativeNegative = Color(0xFFFE3D16)

    // 앱 화면 (주황 테마)
    val AppBackground = AppPalette.WarmGray100
    val BrandOrange = AppPalette.Orange500
    val MusicAccent = AppPalette.Orange500
    val SongIconBackground = Color(0xFFFFF3D6)
    val StageBackground = Color(0xFFFFF4E9)
    val StageFloor = Color(0xFFFFE7CF)
    val SpeechBubbleShadow = Color(0xFFFFE5C2)
    // 본문 (제목 아래 설명 글자)
    val TextWarm = AppPalette.WarmBlack700
    val TextWarmSecondary = AppPalette.WarmBlack700
    // 만들기·보관함 탭
    val SegmentedBackground = AppPalette.WarmGray200
    val ButtonSwitchEnabled = AppPalette.WarmWhite100
    val SegmentedUnselectedText = AppPalette.WarmBlack600

    // 무대 위 말풍선·안내 칩
    val StageBubbleBackground = AppPalette.WarmWhite100
    val HintChipText = AppPalette.WarmBlack900
    val Dim = Color(0x66131417)
    val PlayheadLine = Color(0xFFC9C9C9)
    val PlayheadDot = Color(0xFFD9D9D9)

    // 따뜻한 팝업 (멤버 삭제 확인 등)
    val PopupWarmBackground = AppPalette.WarmWhite100
    val PopupWarmBorder = AppPalette.WarmGray200
    val TextWarmStrong = AppPalette.WarmBlack1000
    val TextWarmBody = AppPalette.WarmGray900
    val ButtonWarmSecondary = AppPalette.WarmGray200

    // 보호자 동의 항목 (S00)
    val ConsentItemsBackground = AppPalette.WarmGray100
    val ConsentItemDivider = AppPalette.WarmGray300

    // 친구 고르기 (S01-1)
    val CharacterLockedName = AppPalette.WarmGray800
    val CharacterLockedLabel = AppPalette.WarmBlack500

    // 속도 칩 (S03)
    val SpeedChipMuted = AppPalette.WarmGray200
    val SpeedChipMutedText = AppPalette.WarmBlack800

    // 토스트
    val ToastBackground = Color(0xFF44464E)

    // 목소리 카드 (연주 중 / 대기)
    val VoiceCardName = AppPalette.WarmBlack1000
    val VoiceCardInstrument = AppPalette.WarmBlack500
    val VoiceCardWaitingName = AppPalette.WarmGray800
    val VoiceCardWaiting = AppPalette.WarmGray500
    val VoiceCardStatusText = AppPalette.WarmWhite100

    // 악기별 (픽토그램 / 배경)
    val DrumPictogram = Color(0xFFFF438E)
    val DrumBackground = Color(0xFFFFEAF3)
    val ShakerPictogram = Color(0xFF0DA2FF)
    val ShakerBackground = Color(0xFFE2F3FF)
    val TambourinePictogram = Color(0xFFFFBC48)
    val TambourineBackground = Color(0xFFFFF3D6)
    val SteeldrumBackground = Color(0xFFECE3FD)
    val WoodblockBackground = Color(0xFFE8FDFC)
    val MaracasBackground = Color(0xFFFFFDEA)
    val UkuleleBackground = Color(0xFFE6FDF4)
    val GuitarBackground = Color(0xFFECFAE9)
    val HarpBackground = Color(0xFFEAF1FE)
    val BellBackground = Color(0xFFFEEDEB)
    val XylophoneBackground = Color(0xFFF2EAFD)
    val PianoBackground = Color(0xFFDCE0FF)
}
