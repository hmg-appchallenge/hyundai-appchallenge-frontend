package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** UI 시안이 나오기 전까지 쓰는 화면 뼈대. */
@Composable
fun ScreenSkeleton(
    screenId: String,
    title: String,
    description: String,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = screenId, style = MaterialTheme.typography.labelLarge)
            Text(text = title, style = MaterialTheme.typography.headlineLarge)
            Text(text = description, style = MaterialTheme.typography.bodyLarge)
            actions()
        }
    }
}

/** 기획서 05번의 Large 버튼(높이 100dp). */
@Composable
fun LargeButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .heightIn(min = 100.dp)
            .widthIn(min = 320.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
    }
}
