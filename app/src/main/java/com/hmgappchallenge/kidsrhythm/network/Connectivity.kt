package com.hmgappchallenge.kidsrhythm.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** 인터넷 연결 상태. [online]은 연결이 바뀔 때마다 바뀐다. */
class ConnectivityState(online: Boolean, private val check: () -> Boolean) {
    var online: Boolean by mutableStateOf(online)
        internal set

    /** "다시 연결"을 누르면 지금 연결을 다시 읽는다. */
    fun recheck() {
        online = check()
    }
}

/** 기본 네트워크가 인터넷을 쓸 수 있는지 지켜본다. 화면을 떠나면 그만 본다. */
@Composable
fun rememberConnectivityState(): ConnectivityState {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    val state = remember(manager) { ConnectivityState(manager.isOnline()) { manager.isOnline() } }
    DisposableEffect(manager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                state.online = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }

            override fun onLost(network: Network) {
                state.online = false
            }
        }
        manager.registerDefaultNetworkCallback(callback)
        onDispose { manager.unregisterNetworkCallback(callback) }
    }
    return state
}

private fun ConnectivityManager.isOnline(): Boolean =
    getNetworkCapabilities(activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
