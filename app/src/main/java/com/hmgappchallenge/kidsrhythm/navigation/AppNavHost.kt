package com.hmgappchallenge.kidsrhythm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hmgappchallenge.kidsrhythm.server.SongGenerator
import com.hmgappchallenge.kidsrhythm.ui.screen.ArchiveScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.BaseBeatScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.CompletedScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.GeneratingScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.HomeScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.IntroScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.RecordingScreen

private object Route {
    const val HOME = "s01_home"
    const val INTRO = "s02_intro"
    const val BASE_BEAT = "s03_base_beat"
    const val RECORDING = "s04_recording"
    const val GENERATING = "s05_generating"
    const val COMPLETED = "s06_completed"
    const val ARCHIVE = "s07_archive"
}

/** 기획서 08번 화면 흐름(S01~S07). */
@Composable
fun AppNavHost(songGenerator: SongGenerator) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.HOME) {
        composable(Route.HOME) {
            HomeScreen(
                onStart = { navController.navigate(Route.INTRO) },
                onOpenArchive = { navController.navigate(Route.ARCHIVE) },
            )
        }
        composable(Route.INTRO) {
            IntroScreen(
                onFinished = {
                    navController.navigate(Route.BASE_BEAT) {
                        popUpTo(Route.INTRO) { inclusive = true }
                    }
                },
            )
        }
        composable(Route.BASE_BEAT) {
            BaseBeatScreen(onStartRecording = { navController.navigate(Route.RECORDING) })
        }
        composable(Route.RECORDING) {
            RecordingScreen(onComplete = { navController.navigate(Route.GENERATING) })
        }
        composable(Route.GENERATING) {
            GeneratingScreen(
                songGenerator = songGenerator,
                onGenerated = {
                    navController.navigate(Route.COMPLETED) {
                        popUpTo(Route.HOME)
                    }
                },
            )
        }
        composable(Route.COMPLETED) {
            CompletedScreen(
                onSave = {
                    navController.navigate(Route.ARCHIVE) {
                        popUpTo(Route.HOME)
                    }
                },
                onMakeAnother = {
                    navController.navigate(Route.BASE_BEAT) {
                        popUpTo(Route.HOME)
                    }
                },
            )
        }
        composable(Route.ARCHIVE) {
            ArchiveScreen(onOpenMake = { navController.popBackStack(Route.HOME, inclusive = false) })
        }
    }
}
