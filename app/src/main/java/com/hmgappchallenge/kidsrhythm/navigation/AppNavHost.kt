package com.hmgappchallenge.kidsrhythm.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hmgappchallenge.kidsrhythm.archive.SongArchive
import com.hmgappchallenge.kidsrhythm.archive.songMetaLabel
import com.hmgappchallenge.kidsrhythm.consent.ConsentGate
import com.hmgappchallenge.kidsrhythm.consent.ConsentStore
import com.hmgappchallenge.kidsrhythm.family.FamilyRepository
import com.hmgappchallenge.kidsrhythm.family.VoiceStatus
import com.hmgappchallenge.kidsrhythm.family.memberDeletedMessage
import com.hmgappchallenge.kidsrhythm.server.SongGenerator
import com.hmgappchallenge.kidsrhythm.ui.component.Toast
import com.hmgappchallenge.kidsrhythm.ui.screen.ArchiveScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.BaseBeatScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.CompletedScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.GeneratingRoute
import com.hmgappchallenge.kidsrhythm.ui.screen.HomeScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.IntroScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.MemberEditorDialog
import com.hmgappchallenge.kidsrhythm.ui.screen.MemberEditorMode
import com.hmgappchallenge.kidsrhythm.ui.screen.RecentSong
import com.hmgappchallenge.kidsrhythm.ui.screen.RecordingRoute
import com.hmgappchallenge.kidsrhythm.ui.screen.VoiceSettingsScreen
import com.hmgappchallenge.kidsrhythm.vehicle.VehicleSignals
import kotlinx.coroutines.delay

private object Route {
    const val HOME = "s01_home"
    const val INTRO = "s02_intro"
    const val BASE_BEAT = "s03_base_beat"
    const val RECORDING = "s04_recording"
    const val ARG_WORD = "word"
    const val GENERATING = "s05_generating?$ARG_WORD={$ARG_WORD}"

    fun generating(word: String?): String =
        if (word.isNullOrBlank()) "s05_generating" else "s05_generating?$ARG_WORD=${Uri.encode(word)}"
    const val COMPLETED = "s06_completed"
    const val ARCHIVE = "s07_archive"
    const val VOICE_SETTINGS = "s02_1_voice_settings"
}

/** 기획서 08번 화면 흐름(S01~S07). S00 동의 팝업은 홈 위에 뜬다. */
@Composable
fun AppNavHost(
    songGenerator: SongGenerator,
    songArchive: SongArchive,
    consentStore: ConsentStore,
    vehicleSignals: VehicleSignals,
    familyRepository: FamilyRepository,
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
                // 흐름도: 동의하면 가족 목소리 등록(S02-1)으로 이어진다
                onAgreeConsent = {
                    consentGate.agree()
                    navController.navigate(Route.VOICE_SETTINGS)
                },
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
            val vehicle by vehicleSignals.state.collectAsState(initial = null)
            val family by familyRepository.members.collectAsState()
            BaseBeatScreen(
                speedKmh = vehicle?.speedKmh ?: 0f,
                family = family,
                onBack = { navController.popBackStack() },
                onOpenVoiceSettings = { navController.navigate(Route.VOICE_SETTINGS) },
                onStartRecording = { navController.navigate(Route.RECORDING) },
            )
        }
        composable(Route.RECORDING) {
            val family by familyRepository.members.collectAsState()
            RecordingRoute(
                family = family,
                onBack = { navController.popBackStack() },
                onComplete = { word -> navController.navigate(Route.generating(word)) },
            )
        }
        composable(
            route = Route.GENERATING,
            arguments = listOf(
                navArgument(Route.ARG_WORD) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) { entry ->
            GeneratingRoute(
                songGenerator = songGenerator,
                word = entry.arguments?.getString(Route.ARG_WORD),
                onBack = { navController.popBackStack() },
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
        composable(Route.VOICE_SETTINGS) {
            val members by familyRepository.members.collectAsState()
            val vehicle by vehicleSignals.state.collectAsState(initial = null)
            var editor by remember { mutableStateOf<MemberEditorMode?>(null) }
            var toast by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(toast) {
                if (toast != null) {
                    delay(TOAST_MILLIS)
                    toast = null
                }
            }
            VoiceSettingsScreen(
                members = members,
                onBack = { navController.popBackStack() },
                onEdit = { editor = MemberEditorMode.Edit(it) },
                onAdd = { editor = MemberEditorMode.Add },
                // 완료하기·나중에 하기 모두 들어온 곳(홈 또는 S03)으로 돌아간다
                onDone = { navController.popBackStack() },
                onLater = { navController.popBackStack() },
                overlay = {
                    editor?.let { mode ->
                        MemberEditorDialog(
                            mode = mode,
                            members = members,
                            // 이름 입력은 정차 중에만 할 수 있다
                            nameEditable = vehicle?.isUxRestricted != true,
                            onDismiss = { editor = null },
                            onSubmit = { draft ->
                                val instrument = requireNotNull(draft.instrument)
                                val hasVoice = draft.voice == VoiceStatus.Registered
                                when (mode) {
                                    MemberEditorMode.Add -> familyRepository.add(draft.name, instrument, hasVoice)
                                    is MemberEditorMode.Edit -> familyRepository.update(
                                        mode.member.copy(name = draft.name, instrument = instrument, hasVoice = hasVoice),
                                    )
                                }
                                editor = null
                            },
                            onDelete = { member ->
                                familyRepository.delete(member.id)
                                editor = null
                                toast = memberDeletedMessage(member.name)
                            },
                        )
                    }
                    toast?.let { Toast(it) }
                },
            )
        }
        composable(Route.ARCHIVE) {
            ArchiveScreen(onOpenMake = { navController.popBackToHome() })
        }
    }
}

/** 토스트를 보여 주는 시간. */
private const val TOAST_MILLIS = 3_000L

private fun NavHostController.popBackToHome() {
    popBackStack(Route.HOME, inclusive = false)
}
