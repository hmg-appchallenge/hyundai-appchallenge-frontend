package com.hmgappchallenge.kidsrhythm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * 디자인 토큰은 [AppColors]·[AppTypography]에 있다.
 * 기존 뼈대 화면의 모양을 바꾸지 않으려고 MaterialTheme에는 아직 연결하지 않았다. 화면 구현(2차) 때 연결한다.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
