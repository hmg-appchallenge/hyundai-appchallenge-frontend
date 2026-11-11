package com.hmgappchallenge.kidsrhythm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.hmgappchallenge.kidsrhythm.archive.FakeSongArchive
import com.hmgappchallenge.kidsrhythm.character.SharedPreferencesCharacterStore
import com.hmgappchallenge.kidsrhythm.consent.SharedPreferencesConsentStore
import com.hmgappchallenge.kidsrhythm.family.InMemoryFamilyRepository
import com.hmgappchallenge.kidsrhythm.navigation.AppNavHost
import com.hmgappchallenge.kidsrhythm.server.FakeSongGenerator
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTheme
import com.hmgappchallenge.kidsrhythm.vehicle.FakeVehicleSignals

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // AI 서버 API, 보관함 저장소, 차량 신호가 정해지면 실제 구현으로 교체한다.
                val songGenerator = remember { FakeSongGenerator() }
                val songArchive = remember { FakeSongArchive() }
                val consentStore = remember { SharedPreferencesConsentStore(applicationContext) }
                val vehicleSignals = remember { FakeVehicleSignals() }
                val familyRepository = remember { InMemoryFamilyRepository() }
                val characterStore = remember { SharedPreferencesCharacterStore(applicationContext) }
                AppNavHost(
                    songGenerator = songGenerator,
                    songArchive = songArchive,
                    consentStore = consentStore,
                    vehicleSignals = vehicleSignals,
                    familyRepository = familyRepository,
                    characterStore = characterStore,
                )
            }
        }
    }
}
