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
import com.hmgappchallenge.kidsrhythm.AppServices
import com.hmgappchallenge.kidsrhythm.archive.songMetaLabel
import com.hmgappchallenge.kidsrhythm.audio.VoiceEnrollment
import com.hmgappchallenge.kidsrhythm.consent.ConsentGate
import com.hmgappchallenge.kidsrhythm.consent.ConsentStore
import com.hmgappchallenge.kidsrhythm.family.VoiceStatus
import com.hmgappchallenge.kidsrhythm.family.memberDeletedMessage
import com.hmgappchallenge.kidsrhythm.ui.component.Toast
import com.hmgappchallenge.kidsrhythm.ui.screen.ArchiveRoute
import com.hmgappchallenge.kidsrhythm.ui.screen.BaseBeatScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.CharacterPickerScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.CompletedRoute
import com.hmgappchallenge.kidsrhythm.ui.screen.GeneratingRoute
import com.hmgappchallenge.kidsrhythm.ui.screen.HomeScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.IntroScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.MemberEditorDialog
import com.hmgappchallenge.kidsrhythm.ui.screen.MemberEditorMode
import com.hmgappchallenge.kidsrhythm.ui.screen.RecentSong
import com.hmgappchallenge.kidsrhythm.ui.screen.RecordingRoute
import com.hmgappchallenge.kidsrhythm.ui.screen.ReplayRoute
import com.hmgappchallenge.kidsrhythm.ui.screen.VoiceSettingsScreen
import com.hmgappchallenge.kidsrhythm.vehicle.VehicleState
import kotlinx.coroutines.delay

private object Route {
    const val HOME = "s01_home"
    const val INTRO = "s02_intro"
    const val BASE_BEAT = "s03_base_beat"
    const val RECORDING = "s04_recording"
    const val ARG_SONG = "songId"
    const val ARG_WORD = "word"
    const val GENERATING = "s05_generating/{$ARG_SONG}?$ARG_WORD={$ARG_WORD}"

    fun generating(songId: String, word: String?): String =
        if (word.isNullOrBlank()) "s05_generating/$songId" else "s05_generating/$songId?$ARG_WORD=${Uri.encode(word)}"
    const val COMPLETED = "s06_completed"
    const val ARCHIVE = "s07_archive"
    const val ARG_ID = "id"
    const val REPLAY = "s07_1_replay/{$ARG_ID}"

    fun replay(id: Long) = "s07_1_replay/$id"
    const val VOICE_SETTINGS = "s02_1_voice_settings"
    const val CHARACTER_PICKER = "s01_1_character_picker"
}

/** S02 시작 음성 안내 (기획서 S02 문구). */
private const val INTRO_GUIDE = "음악 한번 만들어볼까?"

/** 기획서 08번 화면 흐름(S01~S07). S00 동의 팝업은 홈 위에 뜬다. */
@Composable
fun AppNavHost(services: AppServices, consentStore: ConsentStore) {
    val navController = rememberNavController()
    // "나중에"로 닫은 상태가 다른 화면에 다녀와도 유지되도록 화면 밖에서 들고 있는다
    val consentGate = remember(consentStore) { ConsentGate(consentStore) }
    val vehicle by services.vehicle.state.collectAsState(initial = null)
    val family by services.family.members.collectAsState()
    // 같이 놀 친구. 홈, 친구 고르기, S06 완성이 함께 본다
    var character by remember { mutableStateOf(services.characterStore.selected) }

    NavHost(navController = navController, startDestination = Route.HOME) {
        composable(Route.HOME) {
            val songs by services.archive.songs.collectAsState()
            val latest = songs.maxByOrNull { it.createdAtMillis }
            LaunchedEffect(Unit) {
                services.beatPlayer.stop()
                services.player.pause()
            }
            HomeScreen(
                character = character,
                recentSong = latest?.let { RecentSong(it.title, songMetaLabel(it, System.currentTimeMillis())) },
                isConsentVisible = consentGate.isDialogVisible,
                onStart = { if (consentGate.requestStart()) navController.navigate(Route.INTRO) },
                onOpenArchive = { navController.navigate(Route.ARCHIVE) },
                onOpenVoiceSettings = { navController.navigate(Route.VOICE_SETTINGS) },
                onOpenCharacterPicker = { navController.navigate(Route.CHARACTER_PICKER) },
                // TODO(기획): 설정(S08) 화면을 만들면 연결한다
                onOpenSettings = {},
                onPlayRecent = { latest?.let { navController.navigate(Route.replay(it.id)) } },
                // 흐름도: 동의하면 가족 목소리 등록(S02-1)으로 이어진다
                onAgreeConsent = {
                    consentGate.agree()
                    navController.navigate(Route.VOICE_SETTINGS)
                },
                onDismissConsent = consentGate::dismiss,
            )
        }
        composable(Route.INTRO) {
            // 스피커로 시작 안내를 들려준다 (S02 "음악 한번 만들어볼까?")
            LaunchedEffect(Unit) { services.voiceGuide.speak(INTRO_GUIDE) }
            IntroScreen(
                onFinished = {
                    navController.navigate(Route.BASE_BEAT) {
                        popUpTo(Route.INTRO) { inclusive = true }
                    }
                },
                onCancel = {
                    services.voiceGuide.stop()
                    navController.popBackToHome()
                },
                onOpenArchive = {
                    navController.navigate(Route.ARCHIVE) {
                        popUpTo(Route.HOME)
                    }
                },
            )
        }
        composable(Route.BASE_BEAT) {
            val speed = vehicle?.speedKmh ?: 0f
            // 차가 만드는 비트를 바로 들려준다. 속도 구간이 바뀌면 비트도 바뀐다
            LaunchedEffect(speed) { services.beatPlayer.playForSpeed(speed) }
            BaseBeatScreen(
                speedKmh = speed,
                family = family,
                onBack = { navController.popBackStack() },
                onOpenVoiceSettings = { navController.navigate(Route.VOICE_SETTINGS) },
                onStartRecording = { navController.navigate(Route.RECORDING) },
            )
        }
        composable(Route.RECORDING) {
            RecordingRoute(
                family = family,
                services = services,
                speedKmh = vehicle?.speedKmh ?: 0f,
                outsideTemperature = vehicle?.outsideTemperatureCelsius,
                onBack = { navController.popBackStack() },
                onComplete = { songId, word ->
                    navController.navigate(Route.generating(songId, word)) {
                        popUpTo(Route.BASE_BEAT)
                    }
                },
            )
        }
        composable(
            route = Route.GENERATING,
            arguments = listOf(
                navArgument(Route.ARG_SONG) { type = NavType.StringType },
                navArgument(Route.ARG_WORD) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) { entry ->
            GeneratingRoute(
                services = services,
                songId = entry.arguments?.getString(Route.ARG_SONG).orEmpty(),
                word = entry.arguments?.getString(Route.ARG_WORD),
                onBack = { navController.popBackStack() },
                onGenerated = {
                    navController.navigate(Route.COMPLETED) {
                        popUpTo(Route.HOME)
                    }
                },
                onRetry = {
                    navController.navigate(Route.RECORDING) {
                        popUpTo(Route.BASE_BEAT)
                    }
                },
            )
        }
        composable(Route.COMPLETED) {
            CompletedRoute(
                services = services,
                family = family,
                friend = character,
                onSaved = {
                    navController.navigate(Route.ARCHIVE) {
                        popUpTo(Route.HOME)
                    }
                },
                onMakeAnother = {
                    navController.navigate(Route.BASE_BEAT) {
                        popUpTo(Route.HOME)
                    }
                },
                // S06-1 친구 바꾸기는 홈의 친구 고르기(S01-1)와 같은 화면을 쓴다
                onChangeFriend = { navController.navigate(Route.CHARACTER_PICKER) },
                onBack = { navController.popBackToHome() },
            )
        }
        composable(Route.VOICE_SETTINGS) {
            var editor by remember { mutableStateOf<MemberEditorMode?>(null) }
            val enrollment = remember { VoiceEnrollment(services.newRecorder(), services.songService) }
            var toast by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(toast) {
                if (toast != null) {
                    delay(TOAST_MILLIS)
                    toast = null
                }
            }
            VoiceSettingsScreen(
                members = family,
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
                            members = family,
                            // 이름 입력은 정차 중에만 할 수 있다
                            nameEditable = vehicle.isStopped(),
                            onDismiss = { editor = null },
                            onSubmit = { draft ->
                                val instrument = requireNotNull(draft.instrument)
                                val hasVoice = draft.voice == VoiceStatus.Registered
                                val embedding = if (hasVoice) draft.embedding else null
                                when (mode) {
                                    MemberEditorMode.Add -> services.family.add(draft.name, instrument, hasVoice, embedding)
                                    is MemberEditorMode.Edit -> services.family.update(
                                        mode.member.copy(name = draft.name, instrument = instrument, hasVoice = hasVoice, voiceEmbedding = embedding),
                                    )
                                }
                                editor = null
                            },
                            onDelete = { member ->
                                services.family.delete(member.id)
                                editor = null
                                toast = memberDeletedMessage(member.name)
                            },
                            enrollment = enrollment,
                            onPreviewInstrument = services.sounds::preview,
                        )
                    }
                    toast?.let { Toast(it) }
                },
            )
        }
        composable(Route.CHARACTER_PICKER) {
            CharacterPickerScreen(
                current = character,
                onConfirm = {
                    services.characterStore.select(it)
                    character = it
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.ARCHIVE) {
            ArchiveRoute(
                services = services,
                onOpenMake = { navController.popBackToHome() },
                onOpenSong = { navController.navigate(Route.replay(it)) },
            )
        }
        composable(
            route = Route.REPLAY,
            arguments = listOf(navArgument(Route.ARG_ID) { type = NavType.LongType }),
        ) { entry ->
            ReplayRoute(
                services = services,
                songId = entry.arguments?.getLong(Route.ARG_ID) ?: 0L,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

/** 주행 제한이 없을 때(정차 중)만 이름을 입력할 수 있다. */
private fun VehicleState?.isStopped(): Boolean = this?.isUxRestricted != true

/** 토스트를 보여 주는 시간. */
private const val TOAST_MILLIS = 3_000L

private fun NavHostController.popBackToHome() {
    popBackStack(Route.HOME, inclusive = false)
}
