package com.hmgappchallenge.kidsrhythm

import android.Manifest
import android.car.Car
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.hmgappchallenge.kidsrhythm.consent.SharedPreferencesConsentStore
import com.hmgappchallenge.kidsrhythm.navigation.AppNavHost
import com.hmgappchallenge.kidsrhythm.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private val services by lazy { AppServices(applicationContext) }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 녹음(마이크)과 차량 속도(비트)는 처음 한 번 묻는다. 거절해도 앱은 열리고, 녹음 화면에서 다시 묻는다
        val missing = listOf(Manifest.permission.RECORD_AUDIO, Car.PERMISSION_SPEED)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
        setContent {
            AppTheme {
                val consentStore = remember { SharedPreferencesConsentStore(applicationContext) }
                AppNavHost(services = services, consentStore = consentStore)
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) services.release()
        super.onDestroy()
    }
}
