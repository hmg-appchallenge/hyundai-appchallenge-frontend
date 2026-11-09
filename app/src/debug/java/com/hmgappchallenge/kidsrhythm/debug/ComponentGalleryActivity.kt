package com.hmgappchallenge.kidsrhythm.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hmgappchallenge.kidsrhythm.ui.component.Instrument
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentBadge
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentOption
import com.hmgappchallenge.kidsrhythm.ui.component.InstrumentPictogram
import com.hmgappchallenge.kidsrhythm.ui.component.Mascot
import com.hmgappchallenge.kidsrhythm.ui.component.MascotCharacter
import com.hmgappchallenge.kidsrhythm.ui.component.MascotPose
import com.hmgappchallenge.kidsrhythm.ui.component.VoiceCard
import com.hmgappchallenge.kidsrhythm.ui.component.VoiceCardState
import com.hmgappchallenge.kidsrhythm.ui.theme.AppColors
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTypography

/**
 * 공용 컴포넌트를 모아 보는 디버그 화면.
 * `adb shell am start -n com.hmgappchallenge.kidsrhythm/.debug.ComponentGalleryActivity --es section <badges|cards|mascots>`
 */
class ComponentGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val section = intent.getStringExtra("section") ?: "badges"
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.BackgroundPrimary)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text("gallery: $section", style = AppTypography.LabelSmall, color = AppColors.TextDarkTertiary)
                when (section) {
                    "cards" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            VoiceCard("엄마", Instrument.DRUM, VoiceCardState.Playing)
                            VoiceCard("엄마", Instrument.DRUM, VoiceCardState.Waiting)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Instrument.entries.forEach { InstrumentOption(it, name = "Name", season = "Season", onClick = {}) }
                        }
                    }
                    "mascots" -> MascotPose.entries.forEach { pose ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MascotCharacter.entries.forEach { Mascot(it, pose, Modifier.width(300.dp)) }
                        }
                    }
                    else -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Instrument.entries.forEach { InstrumentBadge(it) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            Instrument.entries.forEach {
                                InstrumentPictogram(it)
                            }
                        }
                    }
                }
            }
        }
    }
}
