package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

private val ToastShape = RoundedCornerShape(16.dp)

/** 잠깐 보여 주는 안내 글. Pleos 토스트 모양으로 앱 창 아래 가운데에 뜬다. 언제 사라질지는 부르는 쪽이 정한다. */
@Composable
fun BoxScope.Toast(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .align(Alignment.BottomCenter)
            // 화면 아래 큰 버튼(높이 100)을 가리지 않도록 그 위에 띄운다
            .padding(bottom = 180.dp)
            .clip(ToastShape)
            .background(AppColors.ToastBackground)
            .padding(horizontal = 40.dp, vertical = 24.dp),
    ) {
        Text(text = text, style = AppTypography.BodyLargeStrong, color = AppColors.BasicStaticLight100, maxLines = 1)
    }
}

@Preview(widthDp = 800, heightDp = 300)
@Composable
private fun ToastPreview() {
    Box { Toast("엄마를 삭제했어요") }
}
