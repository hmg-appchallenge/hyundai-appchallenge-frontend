package com.hmgappchallenge.kidsrhythm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** 디자인 토큰이 정해지면 색상·타이포그래피를 이곳에 정의한다. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
