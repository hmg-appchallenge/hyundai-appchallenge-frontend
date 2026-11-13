package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.settings.RecordingBeatLevel
import com.hmgappchallenge.kidsrhythm.settings.SongViewMode
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.PopupOverlay
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.component.WarmConfirmPopup
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val GroupShape = RoundedCornerShape(32.dp)
private val ActionShape = RoundedCornerShape(20.dp)
private val SwitchShape = RoundedCornerShape(16.dp)
private val OptionShape = RoundedCornerShape(32.dp)
private val ThumbnailShape = RoundedCornerShape(20.dp)

/** 팝업 선택지 부제 (22/28 Regular). 공용 토큰에 없는 크기라 여기서만 쓴다. */
private val OptionCaptionStyle = AppTypography.LabelMedium.copy(fontSize = 22.sp)
private val GroupLabelStyle = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Bold)

/** S08에 보여 줄 값. */
data class SettingsState(
    val familyCount: Int,
    val characterNickname: String,
    val songViewMode: SongViewMode,
    val autoEnlargeWhenStopped: Boolean,
    val voiceGuideEnabled: Boolean,
    val recordingBeat: RecordingBeatLevel,
    val consentLabel: String,
    val consentAgreed: Boolean,
    val songsLabel: String,
    val hasSongs: Boolean,
    val micGranted: Boolean,
)

/** S08 설정에서 일어나는 일. */
class SettingsActions(
    val onBack: () -> Unit,
    val onOpenVoiceSettings: () -> Unit,
    val onOpenCharacterPicker: () -> Unit,
    val onOpenPrivacy: () -> Unit,
    val onSongViewModeChange: (SongViewMode) -> Unit,
    val onAutoEnlargeChange: (Boolean) -> Unit,
    val onVoiceGuideChange: (Boolean) -> Unit,
    val onRecordingBeatChange: (RecordingBeatLevel) -> Unit,
    val onWithdrawConsent: () -> Unit,
    val onDeleteVoices: () -> Unit,
    val onDeleteSongs: () -> Unit,
    val onMicClick: () -> Unit,
)

private enum class SettingsPopup { ViewMode, RecordingBeat, Withdraw, DeleteVoices, DeleteSongs }

/** S08 설정. 홈 헤더 ⚙에서 들어온다. 왼쪽은 가족·화면·소리, 오른쪽은 개인정보·보관함·앱 정보. */
@Composable
fun SettingsScreen(state: SettingsState, actions: SettingsActions) {
    var popup by rememberSaveable { mutableStateOf<SettingsPopup?>(null) }
    val close = { popup = null }
    SubPage(
        title = "설정",
        onBack = actions.onBack,
        overlay = {
            when (popup) {
                SettingsPopup.ViewMode -> ViewModePopup(state.songViewMode, onCancel = close) {
                    actions.onSongViewModeChange(it)
                    close()
                }
                SettingsPopup.RecordingBeat -> RecordingBeatPopup(state.recordingBeat, onCancel = close) {
                    actions.onRecordingBeatChange(it)
                    close()
                }
                SettingsPopup.Withdraw -> WarmConfirmPopup(
                    title = "보호자 동의를 철회할까요?",
                    message = "철회하면 가족 목소리 정보가 삭제되고,\n녹음과 노래 만들기가 중단돼요. 저장된 노래는 유지돼요.",
                    cancelText = "취소",
                    onCancel = close,
                    confirmText = "동의 철회하기",
                    onConfirm = {
                        actions.onWithdrawConsent()
                        close()
                    },
                    confirmIcon = R.drawable.ic_trash,
                )
                SettingsPopup.DeleteVoices -> WarmConfirmPopup(
                    title = "멤버 목소리 정보를 모두 지울까요?",
                    message = "멤버의 이름 · 목소리 특징 · 악기 설정이 이 차에서 지워져요.\n지우면 되돌릴 수 없어요.",
                    cancelText = "취소",
                    onCancel = close,
                    confirmText = "지우기",
                    onConfirm = {
                        actions.onDeleteVoices()
                        close()
                    },
                    confirmIcon = R.drawable.ic_trash,
                )
                SettingsPopup.DeleteSongs -> WarmConfirmPopup(
                    title = "저장된 노래를 모두 지울까요?",
                    message = "노래 ${state.songsLabel.substringBefore(" ·")}이 이 차에서 지워져요. 지우면 되돌릴 수 없어요.",
                    cancelText = "취소",
                    onCancel = close,
                    confirmText = "지우기",
                    onConfirm = {
                        actions.onDeleteSongs()
                        close()
                    },
                    confirmIcon = R.drawable.ic_trash,
                )
                null -> {}
            }
        },
    ) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // 화면보다 길어지면 열마다 넘겨 본다 (오른쪽 열이 앱 정보까지 들어가면 넘친다)
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                SettingsGroup("가족 · 친구") {
                    SettingsRow("목소리 설정", "가족 추가 · 이름 · 목소리 · 악기", value = "${state.familyCount}명", onClick = actions.onOpenVoiceSettings)
                    SettingsDivider()
                    SettingsRow("캐릭터", "노래가 완성되면 나와서 춤추는 친구", value = state.characterNickname, onClick = actions.onOpenCharacterPicker)
                }
                SettingsGroup("화면") {
                    SettingsRow(
                        "기본 보기 화면 설정",
                        "기본으로 보이는 화면 크기 설정",
                        value = if (state.songViewMode == SongViewMode.Basic) "기본 보기" else "크게 보기",
                        onClick = { popup = SettingsPopup.ViewMode },
                    )
                    SettingsDivider()
                    SettingsToggleRow("정차하면 캐릭터 크게 보기", "정차 중에만 자동으로 크게 보여줘요", state.autoEnlargeWhenStopped, actions.onAutoEnlargeChange)
                }
                SettingsGroup("소리") {
                    SettingsToggleRow("안내 음성", "\"음악 한번 만들어볼까?\" 같은 캐릭터 안내 (녹음 중엔 꺼져요)", state.voiceGuideEnabled, actions.onVoiceGuideChange)
                    SettingsDivider()
                    SettingsRow(
                        "녹음 중 비트 크기",
                        "아이 목소리가 잘 들리도록 비트를 낮춰요",
                        value = state.recordingBeat.label,
                        onClick = { popup = SettingsPopup.RecordingBeat },
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                SettingsGroup("개인정보") {
                    SettingsActionRow(
                        "보호자 동의",
                        state.consentLabel,
                        actionText = if (state.consentAgreed) "동의 철회" else null,
                        onAction = { popup = SettingsPopup.Withdraw },
                    )
                    SettingsDivider()
                    SettingsRow("개인정보 처리방침", "수집 항목 · 보관 기간 · 삭제 방법", onClick = actions.onOpenPrivacy)
                    SettingsDivider()
                    SettingsActionRow(
                        "목소리 정보 모두 지우기",
                        "가족 이름 · 목소리 특징을 이 차에서 지워요",
                        actionText = if (state.familyCount > 0) "모두 지우기" else null,
                        onAction = { popup = SettingsPopup.DeleteVoices },
                    )
                }
                SettingsGroup("권한") {
                    SettingsRow(
                        "마이크",
                        if (state.micGranted) "노래 녹음 · 목소리 등록에 필요해요" else "접근을 허용해야 녹음과 목소리 등록이 가능해요.",
                        value = if (state.micGranted) "허용됨" else "허용 안 함",
                        valueColor = if (state.micGranted) AppColors.SwitchOn else AppColors.InformativeNegative,
                        onClick = actions.onMicClick,
                    )
                }
                SettingsGroup("보관함") {
                    SettingsActionRow(
                        "저장된 노래",
                        state.songsLabel,
                        actionText = if (state.hasSongs) "모두 지우기" else null,
                        onAction = { popup = SettingsPopup.DeleteSongs },
                    )
                }
                SettingsGroup("앱 정보") {
                    SettingsRow("버전", null, value = "0.1 (공모전 데모)", onClick = null)
                    SettingsDivider()
                    // 차량에는 메일 앱이 없어 누를 곳은 없고, 시안처럼 화살표만 둔다
                    SettingsRow("문의하기", TEAM_CONTACT_EMAIL, onClick = null, showChevron = true)
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = GroupLabelStyle, color = AppColors.RecordingWordsLabel)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(GroupShape)
                .background(AppColors.SettingsGroupBackground),
            content = content,
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(AppColors.SettingsDivider),
    )
}

/** 설정 한 줄의 틀: 제목·부제 오른쪽에 [trailing]을 둔다. */
@Composable
private fun SettingsRowFrame(title: String, subtitle: String?, onClick: (() -> Unit)?, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(start = 32.dp, end = 24.dp, top = 20.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = AppTypography.BodyLargeStrong, color = AppColors.TextDarkPrimary)
            if (subtitle != null) Text(subtitle, style = AppTypography.LabelMedium, color = AppColors.SongMeta)
        }
        trailing()
    }
}

/** 값과 › 가 있는 줄. [onClick]이 없으면 값만 보여 준다. */
@Composable
private fun SettingsRow(
    title: String,
    subtitle: String?,
    value: String? = null,
    valueColor: Color = AppColors.TextDarkPrimary,
    onClick: (() -> Unit)?,
    showChevron: Boolean = onClick != null,
) {
    SettingsRowFrame(title, subtitle, onClick) {
        if (value != null) Text(value, style = AppTypography.BodySmallStrong, color = valueColor)
        if (showChevron) {
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = AppColors.SongMeta, modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SettingsRowFrame(title, subtitle, onClick = { onCheckedChange(!checked) }) {
        SettingsSwitch(checked)
    }
}

/** 오른쪽에 빨간 동작 버튼(철회·삭제)이 있는 줄. [actionText]가 없으면 버튼을 숨긴다. */
@Composable
private fun SettingsActionRow(title: String, subtitle: String, actionText: String?, onAction: () -> Unit) {
    SettingsRowFrame(title, subtitle, onClick = null) {
        if (actionText != null) {
            Box(
                modifier = Modifier
                    .height(72.dp)
                    .clip(ActionShape)
                    .background(AppColors.InformativeNegative)
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(actionText, style = AppTypography.BodySmallStrong, color = AppColors.SettingsActionText)
            }
        }
    }
}

/** 켜기·끄기 스위치 (80×48). 켜지면 초록 바탕에 동그라미가 오른쪽으로 간다. */
@Composable
private fun SettingsSwitch(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 80.dp, height = 48.dp)
            .clip(SwitchShape)
            .background(if (checked) AppColors.SwitchOn else AppColors.SettingsDivider),
    ) {
        Box(
            Modifier
                .offset(x = if (checked) 40.dp else 8.dp, y = 8.dp)
                .size(32.dp)
                .background(Color.White, CircleShape),
        )
    }
}

/** 선택지 동그라미. 고르면 주황 원 안에 흰 점, 안 고르면 [offFilled]에 따라 회색 원이나 테두리만. */
@Composable
private fun Radio(selected: Boolean, offFilled: Boolean) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(
                when {
                    selected -> Modifier.background(AppColors.BrandOrange)
                    offFilled -> Modifier.background(AppColors.SpeedChipBackground)
                    else -> Modifier.border(3.dp, AppColors.RadioOffBorder, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Box(Modifier.size(16.dp).background(Color.White, CircleShape))
    }
}

/** 팝업 공통 틀: 제목·설명, 가운데 내용, 취소·저장하기. */
@Composable
private fun BoxScope.ChoicePopup(top: androidx.compose.ui.unit.Dp, title: String, description: String, onCancel: () -> Unit, onSave: () -> Unit, content: @Composable () -> Unit) {
    PopupOverlay(topPadding = top, width = 808.dp, background = AppColors.PopupWarmBackground, borderColor = AppColors.PopupWarmBorder) {
        Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = AppTypography.TitleLarge, color = AppColors.TextDarkPrimary)
                Text(description, style = AppTypography.BodyLargeStrong, color = AppColors.SongMeta)
                content()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LargeButton(text = "취소", onClick = onCancel, icon = R.drawable.ic_close, variant = LargeButtonVariant.Secondary, modifier = Modifier.weight(1f))
                LargeButton(text = "저장하기", onClick = onSave, icon = R.drawable.ic_check, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** S08-1 기본 보기 화면 설정. 미리 보기 그림으로 두 화면 중 하나를 고른다. */
@Composable
private fun BoxScope.ViewModePopup(current: SongViewMode, onCancel: () -> Unit, onSave: (SongViewMode) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(current) }
    ChoicePopup(230.dp, "기본 보기 화면 설정", "노래를 재생 중일 때 기본 화면을 고를 수 있어요.", onCancel, { onSave(selected) }) {
        Row(modifier = Modifier.padding(top = 28.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ViewModeOption(R.drawable.img_view_basic, "기본 보기", "버튼이 운전석 가까이", selected == SongViewMode.Basic, Modifier.weight(1f)) {
                selected = SongViewMode.Basic
            }
            ViewModeOption(R.drawable.img_view_big, "크게 보기", "뒷좌석에서도 잘 보여요", selected == SongViewMode.Enlarged, Modifier.weight(1f)) {
                selected = SongViewMode.Enlarged
            }
        }
    }
}

@Composable
private fun ViewModeOption(@DrawableRes image: Int, title: String, caption: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(OptionShape)
            .background(AppColors.SurfaceBasic)
            .border(if (selected) 4.dp else 2.dp, if (selected) AppColors.BrandOrange else AppColors.BasicAlphaLight100, OptionShape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(218.dp)
                .clip(ThumbnailShape),
        )
        Row(modifier = Modifier.padding(start = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Radio(selected, offFilled = false)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = AppTypography.BodyLargeStrong, color = AppColors.TextDarkPrimary)
                Text(caption, style = OptionCaptionStyle, color = if (selected) AppColors.RecordingWordsLabel else AppColors.TextDarkTertiary)
            }
        }
    }
}

/** S08-2 녹음 중 비트 크기. */
@Composable
private fun BoxScope.RecordingBeatPopup(current: RecordingBeatLevel, onCancel: () -> Unit, onSave: (RecordingBeatLevel) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(current) }
    val options = listOf(
        Triple(RecordingBeatLevel.Small, "작게 (추천)", "아이 목소리가 잘 녹음돼요"),
        Triple(RecordingBeatLevel.Normal, "보통", "비트를 더 크게 들으며 녹음해요"),
        Triple(RecordingBeatLevel.Off, "끄기", "녹음하는 동안은 비트 없이 조용히"),
    )
    ChoicePopup(218.dp, "녹음 중 비트 크기", "녹음할 때 스피커에서 나오는 비트 크기예요.\n작을수록 아이 목소리가 또렷하게 녹음돼요.", onCancel, { onSave(selected) }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(GroupShape)
                .background(AppColors.SettingsGroupBackground)
                .padding(horizontal = 32.dp, vertical = 8.dp),
        ) {
            options.forEachIndexed { index, (level, title, caption) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.RadioButton) { selected = level }
                        .padding(vertical = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Radio(selected == level, offFilled = true)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(title, style = AppTypography.BodyLargeStrong, color = AppColors.TextDarkPrimary)
                        Text(caption, style = OptionCaptionStyle, color = AppColors.SongMeta)
                    }
                }
                if (index < options.lastIndex) Box(Modifier.fillMaxWidth().height(2.dp).background(AppColors.SpeedChipBackground))
            }
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun SettingsScreenPreview() {
    Box(Modifier.fillMaxSize()) {
        SettingsScreen(
            state = SettingsState(3, "부릉이", SongViewMode.Basic, true, true, RecordingBeatLevel.Small, "동의함 · 2026.11.07 · 철회하면 녹음이 꺼져요", true, "4곡 · 12MB · 이 차에만 저장돼요", true, micGranted = false),
            actions = SettingsActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}
