package com.hmgappchallenge.kidsrhythm.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.R
import com.hmgappchallenge.kidsrhythm.audio.SongPlayer
import com.hmgappchallenge.kidsrhythm.server.LyricLine
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin


/** 재생 위치로 지금 부르는 가사 줄을 찾는다. 첫 줄 전이면 -1. */
fun currentLyricIndex(lines: List<LyricLine>, positionMs: Long): Int = lines.indexOfLast { it.startMs <= positionMs }

/** 1:42 같은 재생 시간. */
fun formatClock(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}

/** 가족 목소리에서 나온 단어(명사)는 주황으로 칠한다 (S06 시안의 '사과'). */
fun lyricText(line: LyricLine, highlightColor: Color = AppColors.BrandOrange): AnnotatedString = buildAnnotatedString {
    append(line.text)
    line.highlights.filter { it.kind == "word" && it.start in 0..it.end && it.end <= line.text.length }.forEach {
        addStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold), it.start, it.end)
    }
}

/** 재생 중에는 프레임마다 재생 위치를 읽는다. */
@Composable
fun PlayerTicker(player: SongPlayer) {
    LaunchedEffect(player.isPlaying) {
        while (player.isPlaying) {
            withFrameMillis { player.tick() }
        }
    }
}

/** 받침이 있으면 [withJong], 없으면 [withoutJong]을 붙인다 (콩콩이가, 뭉게가). */
fun String.josa(withJong: String, withoutJong: String): String {
    val c = lastOrNull() ?: return this
    val code = c.code - 0xAC00
    val hasJong = code in 0 until 11172 && code % 28 != 0
    return this + if (hasJong) withJong else withoutJong
}
