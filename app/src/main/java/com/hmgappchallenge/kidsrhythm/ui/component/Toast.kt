package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
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

/**
 * 아이콘과 두 줄 안내가 있는 따뜻한 색 스낵바. 앱 창 아래 가운데, 큰 버튼 위에 뜬다.
 * 언제 사라질지는 부르는 쪽이 정한다.
 */
@Composable
fun BoxScope.NoticeSnackbar(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 259.dp)
            .shadow(40.dp, ToastShape, ambientColor = Color.Black.copy(alpha = 0.24f), spotColor = Color.Black.copy(alpha = 0.24f))
            .clip(ToastShape)
            .background(AppColors.SnackbarBackground)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .size(width = 680.dp, height = 121.dp)
            .padding(start = 32.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = AppColors.SnackbarContent, modifier = Modifier.size(40.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = AppTypography.BodyLargeStrong, color = AppColors.SnackbarContent, maxLines = 1)
            Text(description, style = AppTypography.CaptionMedium, color = AppColors.SnackbarContent, maxLines = 1)
        }
    }
}

@Preview(widthDp = 800, heightDp = 300)
@Composable
private fun ToastPreview() {
    Box { Toast("엄마를 삭제했어요") }
}

@Preview(widthDp = 1000, heightDp = 500)
@Composable
private fun NoticeSnackbarPreview() {
    Box {
        NoticeSnackbar(
            icon = R.drawable.ic_shield,
            title = "보호자 동의가 있어야 노래를 만들 수 있어요",
            description = "아이 목소리를 녹음하기 때문에 동의가 필요해요",
        )
    }
}
