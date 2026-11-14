package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.SubPage
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlinx.coroutines.launch

private val TocShape = RoundedCornerShape(24.dp)
private val ContentShape = RoundedCornerShape(32.dp)

/** 목차 글자 (28/36 Bold), 절 제목 (32/40 ExtraBold), 본문 (28/40 Regular). 공용 토큰에 없는 크기라 여기서만 쓴다. */
private val TocTitleStyle = AppTypography.TitleSmall.copy(fontSize = AppTypography.TitleSmall.fontSize * 28 / 32, lineHeight = AppTypography.TitleSmall.lineHeight * 36 / 40)
private val SectionTitleStyle = AppTypography.TitleSmall.copy(fontWeight = FontWeight.ExtraBold)
private val SectionBodyStyle = AppTypography.TitleSmall.copy(fontSize = AppTypography.TitleSmall.fontSize * 28 / 32, fontWeight = FontWeight.Normal)

/** 문의 메일. S00-1 6. 문의와 S08 문의하기에 같이 쓴다. */
const val TEAM_CONTACT_EMAIL = "drivebeat.team@gmail.com"

/** 처리방침 한 절. 목차에는 [tocTitle]이 들어간다 (6. 문의는 목차에 없다). */
data class PolicySection(val tocTitle: String?, val title: String, val body: String)

// 실제 동작(docs/API.md "기본 원칙")과 같게 쓴다. 서버·외부 AI로 보내는 것이 바뀌면 여기도 함께 고친다
val PrivacyPolicySections = listOf(
    PolicySection(
        "수집하는 정보",
        "1. 수집하는 정보",
        "아이와 가족의 목소리(녹음), 직접 입력한 가족 이름, 목소리를 구분하기 위한 목소리 특징 정보, 노래 빠르기와 분위기를 정하기 위한 차량 정보(속도·바깥 온도·출발 시각)를 써요. 차량 정보로는 누구인지 알 수 없어요.",
    ),
    PolicySection(
        "어디에 쓰나요",
        "2. 어디에 쓰나요",
        "가사와 노래를 만들고, 가족 목소리마다 고른 악기를 붙이는 데에만 써요. 광고에 쓰지 않고, 저희가 AI를 학습시키는 데에도 쓰지 않아요.",
    ),
    PolicySection(
        "어디서 처리하나요",
        "3. 어디서 처리하나요",
        "녹음과 목소리 등록 소리는 팀이 운영하는 노래 생성 서버로 암호화해 보내 글자로 바꿔요. 이 서버는 미국 Hugging Face의 클라우드(Spaces)에서 돌아가요(처리 위탁 · 국외 이전). " +
            "목소리 특징은 서버가 계산만 하고 저장하지 않아요. 가족 이름은 서버로 보내지 않아요.\n\n" +
            "가사와 노래를 만들 때는 받아 쓴 문장, 들린 단어, 계절·빠르기 같은 글자 정보만 미국의 외부 AI 서비스로 보내요(국외 이전). " +
            "Anthropic(Claude)은 가사와 제목을, ElevenLabs는 반주와 노래를 만들어요. 녹음 소리, 목소리 특징, 가족 이름은 보내지 않아요.",
    ),
    PolicySection(
        "얼마나 보관하나요",
        "4. 얼마나 보관하나요",
        "녹음 원본은 노래가 완성되면 서버에서 바로 지워요. 실패하거나 그만둬도 바로 지워요. 목소리 등록 소리는 특징을 계산한 직후 지워요. 완성곡은 앱이 받아 간 뒤 서버에서 지우고, 받아 가지 못해도 10분 뒤 자동으로 지워요.\n\n" +
            "가족 이름과 목소리 특징, 완성곡은 이 차에만 저장돼요. 가족을 지우면 그 가족의 이름과 목소리 특징이 바로 지워지고, 노래는 보관함이나 설정에서 따로 지울 수 있어요.",
    ),
    PolicySection(
        "보호자의 권리",
        "5. 보호자의 권리",
        "만 14세 미만 아동의 정보는 보호자(법정대리인)의 동의를 받아요. 설정에서 언제든 동의를 철회할 수 있어요. 철회하면 가족 이름·목소리 특징·악기를 지우고 녹음과 노래 만들기를 멈춰요. 완성곡은 남고, 설정의 \"저장된 노래 › 모두 지우기\"로 지울 수 있어요. 열람·삭제 요청은 아래 메일로 보내 주세요.",
    ),
    PolicySection(null, "6. 문의", "개인정보 보호책임자: $TEAM_CONTACT_EMAIL"),
)

/**
 * S00-1 개인정보 처리방침. 왼쪽 목차를 누르면 그 절로 가고, 본문을 넘기면 지금 보는 절이 목차에 표시된다.
 * [returnHint]는 확인 버튼 옆 안내 (동의 화면에서 왔는지, 설정에서 왔는지).
 */
@Composable
fun PrivacyPolicyScreen(returnHint: String, onConfirm: () -> Unit, onBack: () -> Unit) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // 목차를 누르면 그 절을 강조한다. 본문이 짧아 아래쪽 절은 맨 위까지 오지 못하므로, 손으로 넘기기 전까지 누른 절을 유지한다
    var tapped by remember { mutableStateOf<Int?>(null) }
    var programmaticScroll by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling -> if (scrolling && !programmaticScroll) tapped = null }
    }
    val visible by remember { derivedStateOf { listState.firstVisibleItemIndex.coerceAtMost(PrivacyPolicySections.lastIndex - 1) } }
    val current = tapped ?: visible
    SubPage(title = "개인정보 처리방침", onBack = onBack) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(modifier = Modifier.width(375.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrivacyPolicySections.forEachIndexed { index, section ->
                    val toc = section.tocTitle ?: return@forEachIndexed
                    val selected = index == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(TocShape)
                            .then(if (selected) Modifier.background(AppColors.PopupWarmBackground).border(3.dp, AppColors.BrandOrange, TocShape) else Modifier)
                            .clickable(role = Role.Button) {
                                tapped = index
                                scope.launch {
                                    programmaticScroll = true
                                    try { listState.animateScrollToItem(index) } finally { programmaticScroll = false }
                                }
                            }
                            .padding(start = 20.dp, end = 16.dp, top = 18.dp, bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${index + 1}", style = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Bold), color = if (selected) AppColors.BrandOrange else AppColors.RecordingWordsLabel)
                        Text(toc, style = TocTitleStyle, color = if (selected) AppColors.TextDarkPrimary else AppColors.HintChipText)
                    }
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(ContentShape)
                    .background(AppColors.SurfaceBasic),
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp),
            ) {
                itemsIndexed(PrivacyPolicySections) { _, section ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(section.title, style = SectionTitleStyle, color = AppColors.TextDarkPrimary)
                        Text(section.body, style = SectionBodyStyle, color = AppColors.SongMeta)
                    }
                }
            }
        }
        Row(
            modifier = Modifier.padding(top = 28.dp, start = 399.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LargeButton(
                text = "확인했어요",
                onClick = onConfirm,
                icon = R.drawable.ic_check,
                variant = LargeButtonVariant.Dark,
                modifier = Modifier.width(508.dp),
            )
            Text(returnHint, style = AppTypography.LabelMedium, color = AppColors.SongMeta)
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun PrivacyPolicyScreenPreview() {
    PrivacyPolicyScreen(returnHint = "확인 후 동의 화면으로 돌아가요", onConfirm = {}, onBack = {})
}
