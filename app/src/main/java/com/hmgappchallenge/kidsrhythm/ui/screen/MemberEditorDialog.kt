package com.hmgappchallenge.kidsrhythm.ui.screen

import android.Manifest
import androidx.annotation.DrawableRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.audio.VoiceEnrollment
import com.hmgappchallenge.kidsrhythm.family.FamilyMember
import com.hmgappchallenge.kidsrhythm.family.MAX_NAME_LENGTH
import com.hmgappchallenge.kidsrhythm.family.MemberDraft
import com.hmgappchallenge.kidsrhythm.family.VoiceStatus
import com.hmgappchallenge.kidsrhythm.family.takenInstruments
import com.hmgappchallenge.kidsrhythm.server.ServerException
import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentBadge
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentCategory
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.LoadingSpinner
import com.hmgappchallenge.kidsrhythm.ui.component.PopupOverlay
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlin.math.ceil
import kotlin.math.floor

/** 팝업을 연 목적. 수정이면 고칠 가족을 들고 있다. */
sealed interface MemberEditorMode {
    data object Add : MemberEditorMode
    data class Edit(val member: FamilyMember) : MemberEditorMode
}

private val FieldShape = RoundedCornerShape(24.dp)
private val PromptShape = RoundedCornerShape(32.dp)
private val CategoryChipShape = RoundedCornerShape(16.dp)
private val CardShape = RoundedCornerShape(24.dp)
private val FIELD_BORDER = 3.dp
private val FIELD_IDLE_BORDER = 2.dp
private val CARD_BORDER = 2.dp
private val SELECTED_CARD_BORDER = 4.dp
private const val TAKEN_ALPHA = 0.4f

/** 멤버 삭제 확인 팝업 (너비 808). 앱 창 가운데쯤에 뜬다. */
private val DeleteConfirmWidth = 808.dp
private val DeleteConfirmTop = 402.dp

/** 목소리 영역 칩 글자. 시안이 비율로 줄인 값이라 공용 토큰에 없다. */
private val PromptChipTextStyle = AppTypography.TitleSmall.copy(fontSize = 31.37.sp, lineHeight = 40.33.sp)
private val ListeningChipTextStyle = AppTypography.LabelSmall.copy(fontSize = 20.92.sp, lineHeight = 26.9.sp, fontWeight = FontWeight.Bold)

/** 녹음 중 파형 막대 높이 (앞 10개 뒤로 6개 패턴 반복). */
private val ListeningBarHeights = listOf(11, 21, 31, 17, 37, 25, 13, 32, 21, 11) + listOf(27, 35, 19, 13, 23, 9)
private const val LISTENING_PATTERN_START = 10

/**
 * S02-2 새 가족 추가 · S02-3 가족 수정 팝업. 두 경우 모두 제목은 "새 멤버 추가"다 (디자인 확인).
 * [nameEditable]이 false면 주행 중이라 이름을 고칠 수 없다.
 */
@Composable
fun BoxScope.MemberEditorDialog(
    mode: MemberEditorMode,
    members: List<FamilyMember>,
    nameEditable: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (MemberDraft) -> Unit,
    onDelete: (FamilyMember) -> Unit,
    enrollment: VoiceEnrollment? = null,
    onPreviewInstrument: (Instrument) -> Unit = {},
) {
    var draft by remember(mode) {
        mutableStateOf(if (mode is MemberEditorMode.Edit) MemberDraft.from(mode.member) else MemberDraft())
    }
    var checking by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<VoiceError?>(null) }
    var uploadPending by remember { mutableStateOf(false) }
    // 정해진 시간 동안 마이크로 녹음하고, 시간이 다 되면 서버에서 목소리 특징을 받는다.
    // 녹음이 끝나는 순간 이 효과는 키(recording)가 바뀌어 취소되므로, 서버로 보내기는 새로 시작되는 효과(recording=false)에서 한다
    val recording = draft.voice is VoiceStatus.Recording
    var justRecorded by remember { mutableStateOf(false) }
    LaunchedEffect(recording) {
        if (recording) {
            justRecorded = true
            var last = withFrameMillis { it }
            while (draft.voice is VoiceStatus.Recording) {
                withFrameMillis { now ->
                    draft = draft.tickRecording(now - last)
                    last = now
                }
            }
        } else if (justRecorded) {
            justRecorded = false
            if (enrollment != null && draft.voice == VoiceStatus.Registered) uploadPending = true
        }
    }
    LaunchedEffect(uploadPending) {
        if (!uploadPending || enrollment == null) return@LaunchedEffect
        checking = true
        val result = try {
            enrollment.finish()
        } catch (_: ServerException) {
            null
        }
        checking = false
        if (result != null && result.isOk) {
            draft = draft.copy(embedding = result.embedding)
            voiceError = null
        } else {
            draft = draft.copy(voice = VoiceStatus.None, embedding = null)
            voiceError = voiceErrorText(result?.quality)
        }
        uploadPending = false
    }
    DisposableEffect(enrollment) { onDispose { enrollment?.cancel() } }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && enrollment != null && enrollment.start()) {
            voiceError = null
            draft = draft.startRecording()
        } else if (!granted) {
            voiceError = VoiceError("마이크를 쓸 수 없어요", "목소리를 녹음하려면 마이크 권한이 필요해요")
        }
    }
    val startRecording: () -> Unit = {
        when {
            enrollment == null -> draft = draft.startRecording()
            !enrollment.hasPermission() -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            enrollment.start() -> {
                voiceError = null
                draft = draft.startRecording()
            }
            else -> voiceError = VoiceError("마이크를 쓸 수 없어요", "마이크를 열 수 없어요. 잠시 뒤 다시 녹음해 주세요")
        }
    }
    var confirmingDelete by remember(mode) { mutableStateOf(false) }
    val editingId = (mode as? MemberEditorMode.Edit)?.member?.id
    val taken = remember(members, editingId) { takenInstruments(members, editingId) }

    PopupOverlay(topPadding = 29.dp, background = AppColors.PopupWarmBackground) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
                Text("새 멤버 추가", style = AppTypography.TitleLarge, color = AppColors.TextDarkPrimary)
                Text(
                    "이름을 적고, 목소리를 들려주고, 악기를 골라요. 이름 입력은 정차 중에만 할 수 있어요.",
                    style = AppTypography.BodyMedium,
                    color = AppColors.TextDarkSecondary,
                )
            }
            NameField(
                name = draft.name,
                enabled = nameEditable,
                onNameChange = { draft = draft.withName(it) },
                modifier = Modifier.padding(top = 40.dp),
            )
            VoicePrompt(
                voice = draft.voice,
                checking = checking,
                error = voiceError,
                onStartRecording = startRecording,
                modifier = Modifier.padding(top = 28.dp),
            )
            InstrumentPicker(
                selected = draft.instrument,
                taken = taken,
                onSelect = {
                    draft = draft.copy(instrument = it)
                    onPreviewInstrument(it)
                },
                modifier = Modifier.padding(top = 24.dp),
            )
            Row(
                modifier = Modifier.padding(top = 34.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                when (mode) {
                    MemberEditorMode.Add -> {
                        LargeButton(
                            text = "추가하기",
                            onClick = { onSubmit(draft) },
                            enabled = draft.canSubmit && !checking && !recording,
                            icon = R.drawable.ic_check,
                            textStyle = AppTypography.TitleSmallExtraStrong,
                            modifier = Modifier.weight(1f),
                        )
                        LargeButton(
                            text = "나중에",
                            onClick = onDismiss,
                            icon = R.drawable.ic_close,
                            variant = LargeButtonVariant.Secondary,
                            textStyle = AppTypography.TitleSmallExtraStrong,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    is MemberEditorMode.Edit -> {
                        LargeButton(
                            text = "멤버 삭제하기",
                            // 흔들리는 차 안에서 잘못 누르는 것을 막으려고 한 번 더 묻는다
                            onClick = { confirmingDelete = true },
                            icon = R.drawable.ic_trash,
                            variant = LargeButtonVariant.Secondary,
                            textStyle = AppTypography.TitleSmallExtraStrong,
                            modifier = Modifier.weight(1f),
                        )
                        LargeButton(
                            text = "저장하기",
                            onClick = { onSubmit(draft) },
                            enabled = draft.canSubmit && !checking && !recording,
                            icon = R.drawable.ic_check,
                            textStyle = AppTypography.TitleSmallExtraStrong,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = "닫기",
            tint = AppColors.IconDarkPrimary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                // 시안: 닫기 버튼은 안쪽 여백을 넘어 오른쪽 위 모서리에 붙는다
                .offset(x = 10.dp, y = (-16).dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onDismiss),
        )
    }
    if (mode is MemberEditorMode.Edit && confirmingDelete) {
        DeleteMemberConfirm(
            onCancel = { confirmingDelete = false },
            onConfirm = { onDelete(mode.member) },
        )
    }
}

/** S02-3a 멤버 삭제 확인. 수정 팝업 위에 한 번 더 띄운다. */
@Composable
private fun BoxScope.DeleteMemberConfirm(onCancel: () -> Unit, onConfirm: () -> Unit) {
    PopupOverlay(
        topPadding = DeleteConfirmTop,
        width = DeleteConfirmWidth,
        background = AppColors.PopupWarmBackground,
        borderColor = AppColors.PopupWarmBorder,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("멤버를 삭제할까요?", style = AppTypography.TitleLarge, color = AppColors.TextWarmStrong)
                Text(
                    "삭제한 멤버는 가족 그룹에서 제외되며, 복구할 수 없습니다. 다시 이용하려면 새로 등록해야 합니다.",
                    style = AppTypography.BodyLargeStrong,
                    color = AppColors.TextWarmBody,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LargeButton(
                    text = "취소",
                    onClick = onCancel,
                    icon = R.drawable.ic_close,
                    variant = LargeButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                LargeButton(
                    text = "삭제하기",
                    onClick = onConfirm,
                    icon = R.drawable.ic_trash,
                    variant = LargeButtonVariant.Destructive,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * 이름 입력칸. 누르고 있으면 진한 테두리, 아니면 옅은 테두리다.
 * 주행 중에는 회색 바탕으로 잠그고 키보드를 열지 않는다.
 */
@Composable
private fun NameField(name: String, enabled: Boolean, onNameChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val active = enabled && focused
    // 주행 중 잠금도 입력 중처럼 두꺼운 테두리다
    val borderWidth = if (active || !enabled) FIELD_BORDER else FIELD_IDLE_BORDER
    BasicTextField(
        value = name,
        onValueChange = onNameChange,
        enabled = enabled,
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = AppTypography.TitleMediumNormal.copy(color = AppColors.TextDarkPrimary),
        cursorBrush = SolidColor(AppColors.BrandOrange),
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(FieldShape)
                    .background(if (enabled) AppColors.FieldBackground else AppColors.FieldLockedBackground)
                    // 팝업 바탕도 흰색이라 쉬는 상태에도 옅은 테두리로 칸을 보인다
                    .border(borderWidth, fieldBorderColor(enabled, active), FieldShape)
                    .padding(horizontal = 32.dp + borderWidth),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (!enabled) {
                        Text(
                            "주행 중이라 이름을 입력할 수 없어요. 정차 후 다시 시도해 주세요.",
                            style = AppTypography.TitleMediumNormal,
                            color = AppColors.FieldLockedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        if (name.isEmpty()) {
                            Text("이름을 입력해 주세요", style = AppTypography.TitleMediumNormal, color = AppColors.TextDarkTertiary, maxLines = 1)
                        }
                        innerTextField()
                    }
                }
                if (!enabled) {
                    Image(painterResource(R.drawable.ic_lock), contentDescription = "주행 중 잠김", modifier = Modifier.size(44.dp))
                } else if (name.isNotEmpty()) {
                    Text("${name.length} / $MAX_NAME_LENGTH", style = AppTypography.LabelMedium, color = AppColors.FieldCounter)
                }
                // 지우기 버튼은 입력하는 동안에만 보인다
                if (active && name.isNotEmpty()) {
                    Image(
                        painter = painterResource(R.drawable.ic_clear_text),
                        contentDescription = "이름 지우기",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(enabled = enabled, role = Role.Button) { onNameChange("") },
                    )
                }
            }
        },
    )
}

/** 목소리 등록이 안 됐을 때 보여 줄 제목과 설명. */
private data class VoiceError(val title: String, val message: String)

private fun voiceErrorText(quality: String?): VoiceError = when (quality) {
    // 소리 문제(짧음·작음·잡음)는 시안대로 한 문구로 안내한다
    "too_short", "too_quiet", "noisy" -> VoiceError("목소리를 잘 못 들었어요", "조금 더 크게, 조용할 때 말해 주세요")
    null -> VoiceError("목소리를 확인하지 못했어요", "인터넷 연결을 확인하고 다시 녹음해 주세요")
    else -> VoiceError("목소리를 등록하지 못했어요", "잠시 뒤 다시 녹음해 주세요")
}

/** 이름칸 테두리: 주행 중 잠금은 회색, 입력 중은 주황, 쉬는 상태는 옅은 테두리. */
private fun fieldBorderColor(enabled: Boolean, active: Boolean) = when {
    !enabled -> AppColors.FieldLockedBorder
    active -> AppColors.BrandOrange
    else -> AppColors.BasicAlphaLight100
}

@Composable
private fun VoicePrompt(voice: VoiceStatus, checking: Boolean, error: VoiceError?, onStartRecording: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(253.dp)
            .clip(PromptShape)
            .background(AppColors.ButtonWarmSecondary),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (checking) {
            PromptSentence("목소리를 확인하는 중이에요")
            LoadingSpinner(size = 56.dp, contentDescription = null)
            return@Column
        }
        when (voice) {
            VoiceStatus.None -> if (error != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.title, style = AppTypography.TitleMedium, color = AppColors.InformativeNegative, textAlign = TextAlign.Center)
                    Text(error.message, style = AppTypography.BodyMedium, color = AppColors.TextWarm, textAlign = TextAlign.Center)
                }
                PromptChip(text = "목소리 녹음 다시 하기", icon = R.drawable.ic_replay, onClick = onStartRecording)
            } else {
                PromptSentence("“안녕, 우리 같이 노래하자!”")
                PromptChip(text = "목소리 녹음 시작하기", onClick = onStartRecording)
            }
            is VoiceStatus.Recording -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    PromptSentence("“안녕, 우리 같이 노래하자!”")
                    ListeningWaveform()
                }
                ListeningChip(remainingSeconds = ceil(voice.remainingMillis / 1_000.0).toInt())
            }
            VoiceStatus.Registered -> {
                PromptSentence("목소리가 등록돼 있어요")
                PromptChip(text = "목소리 다시 등록하기", onClick = onStartRecording)
            }
        }
    }
}

@Composable
private fun PromptSentence(text: String) {
    Text(text, style = AppTypography.TitleMedium, color = AppColors.TextDarkPrimary, textAlign = TextAlign.Center)
}

@Composable
private fun PromptChip(text: String, onClick: () -> Unit, @DrawableRes icon: Int = R.drawable.ic_mic) {
    Row(
        modifier = Modifier
            .height(71.7.dp)
            .clip(RoundedCornerShape(17.92.dp))
            .background(AppColors.StageBubbleBackground)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 26.89.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = AppColors.IconDarkPrimary, modifier = Modifier.size(38.dp))
        Text(text, style = PromptChipTextStyle, color = AppColors.TextWarm)
    }
}

@Composable
private fun ListeningChip(remainingSeconds: Int) {
    Row(
        modifier = Modifier
            .height(47.82.dp)
            .clip(RoundedCornerShape(11.95.dp))
            .background(AppColors.StageBubbleBackground)
            .padding(horizontal = 17.93.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(20.dp)
                .background(AppColors.InformativeNegative, CircleShape),
        )
        Text("듣는 중 · ${remainingSeconds}초 남았어요", style = ListeningChipTextStyle, color = AppColors.TextWarm)
    }
}

/** 듣는 중 파형. 막대가 왼쪽으로 계속 흘러간다. */
@Composable
private fun ListeningWaveform() {
    val scrolled by rememberInfiniteTransition(label = "listening").animateFloat(
        initialValue = 0f,
        targetValue = 1_000f,
        animationSpec = infiniteRepeatable(tween(100_000, easing = LinearEasing), RepeatMode.Restart),
        label = "listeningBars",
    )
    Canvas(
        modifier = Modifier
            .width(603.dp)
            .height(53.dp)
            .clipToBounds(),
    ) {
        val barWidth = 7.dp.toPx()
        val pitch = barWidth + 6.dp.toPx()
        val radius = CornerRadius(3.6.dp.toPx())
        val first = floor(scrolled).toInt()
        val shift = (scrolled - first) * pitch
        val count = (size.width / pitch).toInt() + 2
        repeat(count) { slot ->
            val h = barHeightAt(first + slot).dp.toPx()
            drawRoundRect(
                color = AppColors.BrandOrange,
                topLeft = Offset(slot * pitch - shift, size.height / 2 - h / 2),
                size = Size(barWidth, h),
                cornerRadius = radius,
            )
        }
    }
}

private fun barHeightAt(index: Int): Int {
    if (index < LISTENING_PATTERN_START) return ListeningBarHeights[index]
    val pattern = ListeningBarHeights.size - LISTENING_PATTERN_START
    return ListeningBarHeights[LISTENING_PATTERN_START + (index - LISTENING_PATTERN_START) % pattern]
}

@Composable
private fun InstrumentPicker(
    selected: Instrument?,
    taken: Map<Instrument, String>,
    onSelect: (Instrument) -> Unit,
    modifier: Modifier = Modifier,
) {
    var category by remember { mutableStateOf<InstrumentCategory?>(null) }
    val visible = Instrument.entries.filter { category == null || it.category == category }
    Column(modifier = modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("악기 고르기 · 누르면 3초 미리 듣기", style = AppTypography.BodySmallStrong, color = AppColors.TextDarkSecondary)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryChip("전체 ${Instrument.entries.size}", selected = category == null) { category = null }
                InstrumentCategory.entries.forEach { c ->
                    CategoryChip(c.label, selected = category == c) { category = c }
                }
            }
            val scroll = rememberScrollState()
            // 끝까지 넘겨 더 볼 악기가 없으면 오른쪽 흐림을 서서히 지운다
            val fadeAlpha by animateFloatAsState(if (scroll.canScrollForward) 1f else 0f, label = "instrumentFade")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(scroll),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    visible.forEach { instrument ->
                        val takenBy = taken[instrument]
                        InstrumentCard(
                            instrument = instrument,
                            isSelected = instrument == selected,
                            takenBy = takenBy,
                            // 누르면 고르면서 3초 미리 듣기를 재생한다 (onSelect → onPreviewInstrument)
                            onClick = { if (takenBy == null) onSelect(instrument) },
                        )
                    }
                }
                // 오른쪽에 악기가 더 있다는 표시
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(160.dp)
                        .fillMaxHeight()
                        .alpha(fadeAlpha)
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, AppColors.BackgroundPopup))),
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(64.dp)
            .clip(CategoryChipShape)
            .background(if (selected) AppColors.PopupWarmBackground else AppColors.ButtonWarmSecondary)
            .then(if (selected) Modifier.border(CARD_BORDER, AppColors.BrandOrange, CategoryChipShape) else Modifier)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 24.dp + CARD_BORDER),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = AppTypography.BodySmallStrong, color = if (selected) AppColors.TextDarkPrimary else AppColors.TextDarkQuaternary)
    }
}

@Composable
private fun InstrumentCard(instrument: Instrument, isSelected: Boolean, takenBy: String?, onClick: () -> Unit) {
    val border = if (isSelected) SELECTED_CARD_BORDER else CARD_BORDER
    Box(
        modifier = Modifier
            .width(200.dp)
            .alpha(if (takenBy != null) TAKEN_ALPHA else 1f)
            .clip(CardShape)
            .background(AppColors.PopupWarmBackground)
            .border(border, if (isSelected) AppColors.BrandOrange else AppColors.PopupWarmBorder, CardShape)
            .clickable(enabled = takenBy == null, role = Role.RadioButton, onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InstrumentBadge(instrument, size = if (isSelected) 78.dp else 72.dp)
            Text(instrument.label, style = AppTypography.BodySmallStrong, color = AppColors.TextDarkPrimary)
            Text(
                text = when {
                    takenBy != null -> "$takenBy 사용 중"
                    isSelected -> "선택됨"
                    else -> "미리 듣기"
                },
                style = AppTypography.LabelSmall,
                color = if (isSelected) AppColors.TextWarm else AppColors.TextDarkTertiary,
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .offset(11.dp, 10.dp)
                    .size(40.dp)
                    .background(AppColors.BrandOrange, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = AppColors.BasicStaticLight100, modifier = Modifier.size(28.dp))
            }
        }
    }
}
