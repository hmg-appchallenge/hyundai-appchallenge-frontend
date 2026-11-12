package com.hmgappchallenge.kidsrhythm.recording

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** 마이크 권한 상태. [granted]는 화면에 돌아올 때마다 다시 읽는다. */
class MicPermissionState(granted: Boolean, private val onRequest: () -> Unit) {
    var granted: Boolean by mutableStateOf(granted)
        internal set

    /** 허용 전이면 시스템 권한 창을 띄우고, 이미 허용했으면 앱 정보 화면을 연다. */
    fun request() = onRequest()
}

/**
 * S08 마이크 줄에 쓰는 권한 상태.
 * - 허용 전: 시스템 권한 창(S08-6a)을 띄운다. 시스템이 더 묻지 않는 상태(다시 묻지 않음)면 앱 정보 화면을 연다
 * - 허용됨: 끄려면 시스템에서 바꿔야 해서 앱 정보 화면을 연다
 */
@Composable
fun rememberMicPermissionState(): MicPermissionState {
    val context = LocalContext.current
    lateinit var state: MicPermissionState
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        state.granted = granted
        val activity = context.findActivity()
        if (!granted && activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
            context.openAppDetails()
        }
    }
    state = remember {
        MicPermissionState(context.hasMicPermission()) {
            if (state.granted) context.openAppDetails() else launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state.granted = context.hasMicPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return state
}

private fun Context.hasMicPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

private fun Context.openAppDetails() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    // 차량에 따라 앱 정보 화면이 없을 수 있어, 없으면 아무것도 하지 않는다
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
