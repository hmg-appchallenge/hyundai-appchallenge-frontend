package com.hmgappchallenge.kidsrhythm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.hmgappchallenge.kidsrhythm.navigation.AppNavHost
import com.hmgappchallenge.kidsrhythm.server.FakeSongGenerator
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // AI 서버 API가 정해지면 실제 구현으로 교체한다.
                val songGenerator = remember { FakeSongGenerator() }
                AppNavHost(songGenerator = songGenerator)
            }
        }
    }
}
