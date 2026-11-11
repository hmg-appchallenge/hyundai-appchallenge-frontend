package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

enum class AppTab(val label: String) { Make("만들기"), Archive("보관함") }

/** 위쪽 1/3은 스테이지 색, 아래로 갈수록 투명해지는 스테이지 배경. */
val StageFadeBackground = Brush.verticalGradient(
    0.333f to AppColors.StageBackground,
    0.715f to Color.Transparent,
)

private val WindowShape = RoundedCornerShape(40.dp)
private val StageShape = RoundedCornerShape(48.dp)
private val SegmentShape = RoundedCornerShape(32.dp)
private val SegmentItemShape = RoundedCornerShape(24.dp)
private val BackButtonShape = RoundedCornerShape(20.dp)

/** 앱 창: 둥근 배경 위에 높이 100의 헤더와 본문을 놓는다. [overlay]는 창 전체를 덮는 팝업 자리다. */
@Composable
fun AppWindow(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(WindowShape)
            .background(AppColors.AppBackground),
    ) {
        Column(modifier = Modifier.padding(start = 64.dp, end = 64.dp, top = 32.dp, bottom = 64.dp)) {
            Box(modifier = Modifier.height(100.dp), contentAlignment = Alignment.CenterStart) { header() }
            Column(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxSize(),
                content = content,
            )
        }
        overlay()
    }
}

/** 만들기/보관함 탭이 있는 화면: 왼쪽 스테이지, 오른쪽 패널. */
@Composable
fun AppPage(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    stageBackground: Brush,
    stage: @Composable BoxScope.() -> Unit,
    panel: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    AppWindow(
        header = { SegmentedTabs(selectedTab, onSelectTab, Modifier.fillMaxHeight()) },
        modifier = modifier,
        overlay = overlay,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(StageShape)
                    .background(stageBackground),
                content = stage,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                content = panel,
            )
        }
    }
}

/** 만들기/보관함 탭만 있고 본문은 창 너비를 다 쓰는 화면 (S07 보관함). */
@Composable
fun TabPage(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    AppWindow(
        header = { SegmentedTabs(selectedTab, onSelectTab, Modifier.fillMaxHeight()) },
        modifier = modifier,
        overlay = overlay,
        content = content,
    )
}

/** 뒤로 버튼과 제목이 있는 하위 화면. 본문은 창 너비를 다 쓴다. */
@Composable
fun SubPage(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    AppWindow(header = { BackHeader(title, onBack) }, modifier = modifier, overlay = overlay, content = content)
}

@Composable
private fun BackHeader(title: String, onBack: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(BackButtonShape)
                .background(AppColors.BasicAlphaLight50)
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_back), contentDescription = "뒤로", tint = AppColors.IconDarkPrimary, modifier = Modifier.size(52.dp))
        }
        Text(text = title, style = AppTypography.TitleLarge, color = AppColors.TextDarkPrimary)
    }
}

@Composable
private fun SegmentedTabs(selected: AppTab, onSelect: (AppTab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(SegmentShape)
            .background(AppColors.SegmentedBackground)
            .padding(8.dp),
    ) {
        AppTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .then(
                        if (isSelected) {
                            Modifier
                                .dropShadow(Color.Black.copy(alpha = 0.16f), 8.dp, 32.dp, 24.dp)
                                .dropShadow(Color.Black.copy(alpha = 0.08f), 2.dp, 2.dp, 24.dp)
                        } else {
                            Modifier
                        },
                    )
                    .clip(SegmentItemShape)
                    .background(if (isSelected) AppColors.ButtonSwitchEnabled else Color.Transparent)
                    .clickable(role = Role.Tab) { if (!isSelected) onSelect(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    style = AppTypography.TitleSmall,
                    color = if (isSelected) AppColors.TextDarkPrimary else AppColors.TextDarkSecondary,
                )
            }
        }
    }
}
