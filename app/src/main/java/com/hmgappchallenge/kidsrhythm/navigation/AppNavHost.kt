package com.hmgappchallenge.kidsrhythm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hmgappchallenge.kidsrhythm.archive.SongArchive
import com.hmgappchallenge.kidsrhythm.archive.songMetaLabel
import com.hmgappchallenge.kidsrhythm.consent.ConsentGate
import com.hmgappchallenge.kidsrhythm.consent.ConsentStore
import com.hmgappchallenge.kidsrhythm.server.SongGenerator
import com.hmgappchallenge.kidsrhythm.ui.screen.ArchiveScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.BaseBeatScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.CompletedScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.GeneratingScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.HomeScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.IntroScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.RecentSong
import com.hmgappchallenge.kidsrhythm.ui.screen.RecordingRoute

private object Route {
    const val HOME = "s01_home"
    const val INTRO = "s02_intro"
    const val BASE_BEAT = "s03_base_beat"
    const val RECORDING = "s04_recording"
    const val GENERATING = "s05_generating"
    const val COMPLETED = "s06_completed"
    const val ARCHIVE = "s07_archive"
}

/** 기획서 08번 화면 흐름(S01~S07). S00 동의 팝업은 홈 위에 뜬다. */
@Composable
fun AppNavHost(
    songGenerator: SongGenerator,
    songArchive: SongArchive,
    consentStore: ConsentStore,
) {
    val navController = rememberNavController()
    // "나중에"로 닫은 상태가 다른 화면에 다녀와도 유지되도록 화면 밖에서 들고 있는다
    val consentGate = remember(consentStore) { ConsentGate(consentStore) }

    NavHost(navController = navController, startDestination = Route.HOME) {
        composable(Route.HOME) {
            val songs by songArchive.songs.collectAsState(initial = emptyList())
            val latest = songs.maxByOrNull { it.createdAtMillis }
            HomeScreen(
                recentSong = latest?.let { RecentSong(it.title, songMetaLabel(it, System.currentTimeMillis())) },
                isConsentVisible = consentGate.isDialogVisible,
                onStart = { if (consentGate.requestStart()) navController.navigate(Route.INTRO) },
                onOpenArchive = { navController.navigate(Route.ARCHIVE) },
                // 플레이어를 구현하면 연결한다
                onPlayRecent = {},
                onAgreeConsent = consentGate::agree,
                onDismissConsent = consentGate::dismiss,
            )
        }
        composable(Route.INTRO) {
            IntroScreen(
                onFinished = {
                    navController.navigate(Route.BASE_BEAT) {
                        popUpTo(Route.INTRO) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackToHome() },
                onOpenArchive = {
                    navController.navigate(Route.ARCHIVE) {
                        popUpTo(Route.HOME)
                    }
                },
            )
        }
        composable(Route.BASE_BEAT) {
            BaseBeatScreen(onStartRecording = { navController.navigate(Route.RECORDING) })
        }
        composable(Route.RECORDING) {
            RecordingRoute(
                onBack = { navController.popBackStack() },
                onComplete = { navController.navigate(Route.GENERATING) },
            )
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
            ArchiveScreen(onOpenMake = { navController.popBackToHome() })
        }
    }
}

private fun NavHostController.popBackToHome() {
    popBackStack(Route.HOME, inclusive = false)
}
