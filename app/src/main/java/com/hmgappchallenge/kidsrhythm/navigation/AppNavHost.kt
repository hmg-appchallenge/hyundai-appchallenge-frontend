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
import com.hmgappchallenge.kidsrhythm.recording.openAppDetails
import com.hmgappchallenge.kidsrhythm.recording.hasMicPermission
import com.hmgappchallenge.kidsrhythm.network.rememberConnectivityState
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.Manifest
import com.hmgappchallenge.kidsrhythm.recording.rememberMicPermissionState
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
import com.hmgappchallenge.kidsrhythm.ui.screen.SettingsState
import com.hmgappchallenge.kidsrhythm.ui.screen.SettingsScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.SplashScreen
import com.hmgappchallenge.kidsrhythm.ui.screen.SettingsActions
import com.hmgappchallenge.kidsrhythm.ui.screen.PrivacyPolicyScreen
import com.hmgappchallenge.kidsrhythm.settings.storageLabel
import com.hmgappchallenge.kidsrhythm.settings.startsEnlarged
import com.hmgappchallenge.kidsrhythm.settings.consentLabel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf

private object Route {
    const val SPLASH = "splash"
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
    const val SETTINGS = "s08_settings"
    const val ARG_FROM = "from"
    const val PRIVACY = "s00_1_privacy/{$ARG_FROM}"

    /** [fromSettings]면 확인 후 설정으로, 아니면 동의 화면으로 돌아간다는 안내를 보여 준다. */
    fun privacy(fromSettings: Boolean) = "s00_1_privacy/${if (fromSettings) "settings" else "consent"}"
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

    NavHost(navController = navController, startDestination = Route.SPLASH) {
        // 앱을 켜면 시작 화면을 잠깐 보여 주고 홈으로 간다. 뒤로 가기로 다시 돌아오지 않게 지운다
        composable(Route.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Route.HOME) { popUpTo(Route.SPLASH) { inclusive = true } }
                },
            )
        }
        composable(Route.HOME) {
            val songs by services.archive.songs.collectAsState()
            val latest = songs.maxByOrNull { it.createdAtMillis }
            LaunchedEffect(Unit) {
                services.beatPlayer.stop()
                services.player.pause()
            }
            val connectivity = rememberConnectivityState()
            // 인터넷이 있어도 노래 서버가 꺼져 있으면 같은 안내를 띄운다. 홈에 들어올 때, 연결이 바뀔 때, 다시 시도할 때 확인한다
            var serverReachable by remember { mutableStateOf(true) }
            var serverCheck by remember { mutableIntStateOf(0) }
            LaunchedEffect(connectivity.online, serverCheck) {
                serverReachable = !connectivity.online || services.songService.isReachable()
            }
            val context = LocalContext.current
            // 마이크 권한이 없으면 시스템 권한 창을 먼저 띄우고, 거절되면 S01 "마이크를 쓸 수 없어요"를 보여 준다
            var micBlocked by remember { mutableStateOf(false) }
            // 목소리를 등록한 가족이 없으면 녹음에서 누가 말했는지 알 수 없어 등록을 먼저 권한다
            var noVoice by remember { mutableStateOf(false) }
            val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) navController.navigate(Route.INTRO) else micBlocked = true
            }
            HomeScreen(
                character = character,
                recentSong = latest?.let { RecentSong(it.title, songMetaLabel(it, System.currentTimeMillis())) },
                isConsentVisible = consentGate.isDialogVisible,
                onStart = {
                    if (consentGate.requestStart()) {
                        if (family.none { it.hasVoice }) {
                            noVoice = true
                        } else if (context.hasMicPermission()) navController.navigate(Route.INTRO) else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onOpenArchive = { navController.navigate(Route.ARCHIVE) },
                onOpenVoiceSettings = { navController.navigate(Route.VOICE_SETTINGS) },
                onOpenCharacterPicker = { navController.navigate(Route.CHARACTER_PICKER) },
                onOpenSettings = { navController.navigate(Route.SETTINGS) },
                onPlayRecent = { latest?.let { navController.navigate(Route.replay(it.id)) } },
                // 흐름도: 동의하면 가족 목소리 등록(S02-1)으로 이어진다
                onAgreeConsent = {
                    consentGate.agree()
                    navController.navigate(Route.VOICE_SETTINGS)
                },
                onDismissConsent = consentGate::dismiss,
                onOpenPrivacy = { navController.navigate(Route.privacy(fromSettings = false)) },
                consentNoticeCount = consentGate.noticeCount,
                onTapConsentNotice = consentGate::openDialog,
                isDisconnected = !connectivity.online || !serverReachable,
                onRetryConnection = {
                    connectivity.recheck()
                    serverCheck++
                },
                micBlocked = micBlocked,
                onDismissMicBlocked = { micBlocked = false },
                onOpenMicSettings = {
                    micBlocked = false
                    context.openAppDetails()
                },
                noVoice = noVoice,
                onDismissNoVoice = { noVoice = false },
                onRegisterVoice = {
                    noVoice = false
                    navController.navigate(Route.VOICE_SETTINGS)
                },
            )
        }
        composable(Route.INTRO) {
            // 스피커로 시작 안내를 들려준다 (S02 "음악 한번 만들어볼까?")
            LaunchedEffect(Unit) { if (services.settings.voiceGuideEnabled) services.voiceGuide.speak(INTRO_GUIDE) }
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
                character = character,
                startEnlarged = startsEnlarged(
                    services.settings.songViewMode,
                    services.settings.autoEnlargeWhenStopped,
                    stopped = vehicle.isStopped(),
                ),
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
                onChangeCharacter = { navController.navigate(Route.CHARACTER_PICKER) },
                onBack = { navController.popBackToHome() },
            )
        }
        composable(Route.VOICE_SETTINGS) {
            // 비트 화면에서 넘어와도 목소리를 녹음하는 동안 비트가 섞이지 않게 끈다. 비트 화면으로 돌아가면 다시 튼다
            LaunchedEffect(Unit) { services.beatPlayer.stop() }
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
        composable(Route.SETTINGS) {
            val songs by services.archive.songs.collectAsState()
            var songBytes by remember { mutableLongStateOf(0L) }
            LaunchedEffect(songs) { songBytes = services.archive.totalBytes() }
            // 동의 저장소는 Compose 상태가 아니라서, 철회하면 이 값을 올려 다시 읽게 한다
            var consentVersion by remember { mutableIntStateOf(0) }
            val agreed = remember(consentVersion) { consentStore.isAgreed }
            val agreedAt = remember(consentVersion) { consentStore.agreedAtMillis }
            val settings = services.settings
            val scope = rememberCoroutineScope()
            val mic = rememberMicPermissionState()
            SettingsScreen(
                state = SettingsState(
                    familyCount = family.size,
                    characterNickname = character.nickname,
                    songViewMode = settings.songViewMode,
                    autoEnlargeWhenStopped = settings.autoEnlargeWhenStopped,
                    voiceGuideEnabled = settings.voiceGuideEnabled,
                    recordingBeat = settings.recordingBeat,
                    consentLabel = consentLabel(agreed, agreedAt),
                    consentAgreed = agreed,
                    songsLabel = "${songs.size}곡 · ${storageLabel(songBytes)} · 이 차에만 저장돼요",
                    hasSongs = songs.isNotEmpty(),
                    micGranted = mic.granted,
                ),
                actions = SettingsActions(
                    onBack = { navController.popBackStack() },
                    onOpenVoiceSettings = { navController.navigate(Route.VOICE_SETTINGS) },
                    onOpenCharacterPicker = { navController.navigate(Route.CHARACTER_PICKER) },
                    onOpenPrivacy = { navController.navigate(Route.privacy(fromSettings = true)) },
                    onSongViewModeChange = settings::updateSongViewMode,
                    onAutoEnlargeChange = settings::updateAutoEnlargeWhenStopped,
                    onVoiceGuideChange = settings::updateVoiceGuideEnabled,
                    onRecordingBeatChange = settings::updateRecordingBeat,
                    // 기획 결정: 철회하면 목소리·이름·악기를 지우고, 완성곡은 남긴다
                    onWithdrawConsent = {
                        services.family.deleteAll()
                        consentGate.withdraw()
                        consentVersion++
                    },
                    onDeleteVoices = { services.family.deleteAll() },
                    onDeleteSongs = {
                        services.player.release()
                        scope.launch { services.archive.deleteAll() }
                    },
                    onMicClick = mic::request,
                ),
            )
        }
        composable(
            route = Route.PRIVACY,
            arguments = listOf(navArgument(Route.ARG_FROM) { type = NavType.StringType }),
        ) { entry ->
            val fromSettings = entry.arguments?.getString(Route.ARG_FROM) == "settings"
            PrivacyPolicyScreen(
                returnHint = if (fromSettings) "확인 후 설정으로 돌아가요" else "확인 후 동의 화면으로 돌아가요",
                onConfirm = { navController.popBackStack() },
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
                driving = !vehicle.isStopped(),
                character = character,
                onBack = { navController.popBackStack() },
                // 다음 곡은 지금 다시 듣기를 바꿔 끼운다 (뒤로 가면 보관함)
                onOpenSong = { id ->
                    navController.navigate(Route.replay(id)) {
                        popUpTo(Route.REPLAY) { inclusive = true }
                    }
                },
                onChangeCharacter = { navController.navigate(Route.CHARACTER_PICKER) },
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
