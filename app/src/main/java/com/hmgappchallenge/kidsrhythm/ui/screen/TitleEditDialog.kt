package com.hmgappchallenge.kidsrhythm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButton
import com.hmgappchallenge.kidsrhythm.ui.component.LargeButtonVariant
import com.hmgappchallenge.kidsrhythm.ui.component.PopupOverlay
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

/** 노래 제목 최대 글자 수 (공백 포함). */
const val MAX_TITLE_LENGTH = 12

private val FieldShape = RoundedCornerShape(24.dp)
private val FIELD_BORDER = 3.dp

/** 입력한 제목을 저장할 수 있는지. 앞뒤 공백만 있는 제목은 안 된다. */
fun isSavableTitle(title: String): Boolean = title.isNotBlank() && title.length <= MAX_TITLE_LENGTH

/** S06-2 노래 제목 바꾸기. 지금 제목을 채워 열고, 12자까지 고칠 수 있다. */
@Composable
fun BoxScope.TitleEditDialog(initial: String, onCancel: () -> Unit, onSave: (String) -> Unit) {
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initial.take(MAX_TITLE_LENGTH), TextRange(initial.length.coerceAtMost(MAX_TITLE_LENGTH))))
    }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    PopupOverlay(
        topPadding = 402.dp,
        width = 808.dp,
        background = AppColors.PopupWarmBackground,
        borderColor = AppColors.PopupWarmBorder,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
            Text("노래 제목 바꾸기", style = AppTypography.TitleLarge, color = AppColors.TextDarkPrimary)
            BasicTextField(
                value = value,
                // 12자를 넘는 입력은 받지 않는다
                onValueChange = { if (it.text.length <= MAX_TITLE_LENGTH) value = it },
                singleLine = true,
                textStyle = AppTypography.TitleMediumNormal.copy(color = AppColors.TextDarkPrimary),
                cursorBrush = SolidColor(AppColors.BrandOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .focusRequester(focus),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(FieldShape)
                            .background(AppColors.PopupWarmBackground)
                            .border(FIELD_BORDER, AppColors.BrandOrange, FieldShape)
                            .padding(start = 26.dp + FIELD_BORDER, end = 20.dp + FIELD_BORDER),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.weight(1f)) { innerTextField() }
                        Text("${value.text.length} / $MAX_TITLE_LENGTH", style = AppTypography.LabelMedium, color = AppColors.FieldCounter)
                        Icon(painterResource(R.drawable.ic_edit), contentDescription = null, tint = AppColors.FieldCounter, modifier = Modifier.size(40.dp))
                    }
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LargeButton(
                    text = "취소하기",
                    onClick = onCancel,
                    icon = R.drawable.ic_close,
                    variant = LargeButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                )
                LargeButton(
                    text = "저장하기",
                    onClick = { onSave(value.text.trim()) },
                    enabled = isSavableTitle(value.text),
                    icon = R.drawable.ic_check,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(widthDp = 1700, heightDp = 1184)
@Composable
private fun TitleEditDialogPreview() {
    Box(Modifier.fillMaxSize()) { TitleEditDialog(initial = "사과 노래", onCancel = {}, onSave = {}) }
}
