package com.hmgappchallenge.kidsrhythm.vehicle

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.drivingstate.CarUxRestrictionsManager
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * AAOS Car API로 실제 속도, 바깥 온도, 주행 제한 상태를 받는다.
 * 에뮬레이터에서는 Pleos Virtual Console이나 adb로 속도를 바꿀 수 있다.
 * 차량 서비스가 없거나(휴대폰 등) 속도 권한이 없으면 [fallback] 값을 쓴다.
 */
class CarVehicleSignals(
    private val context: Context,
    private val fallback: VehicleState = VehicleState(speedKmh = 0f, outsideTemperatureCelsius = null, isUxRestricted = false),
) : VehicleSignals {

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * 디버그 빌드 전용: 앱 내부 폴더의 debug_speed.txt(숫자, km/h)가 있으면 그 속도를 쓴다.
     * 에뮬레이터는 adb로 넣은 속도가 다음 측정 때 0으로 돌아가서, 데모·시험 때 속도를 고정하려고 둔다.
     */
    private val debuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    /** 1초마다 [file]을 읽어 [parse]한 값. 파일이 없으면 null(차량 값을 그대로 쓴다). */
    private fun <T> debugOverride(file: File, parse: (String) -> T?): Flow<T?> = flow {
        while (true) {
            emit(if (file.exists()) parse(file.readText().trim()) else null)
            delay(DEBUG_SPEED_POLL_MS)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    /**
     * 디버그 빌드 전용: debug_driving.txt가 있으면 주행 중(1)·정차(0)를 그 값으로 정한다.
     * 이 에뮬레이터는 adb로 주행 중 화면 제한을 바꿀 수 없어서, 시연 영상·시험 때 주행 화면을 보려고 둔다.
     */
    override val state: Flow<VehicleState> =
        if (!debuggable) {
            carState()
        } else {
            combine(
                carState(),
                debugOverride(File(context.filesDir, DEBUG_SPEED_FILE)) { it.toFloatOrNull()?.coerceIn(0f, 250f) },
                debugOverride(File(context.filesDir, DEBUG_DRIVING_FILE)) { it.toIntOrNull()?.let { v -> v != 0 } },
            ) { car, speed, driving ->
                car.copy(speedKmh = speed ?: car.speedKmh, isUxRestricted = driving ?: car.isUxRestricted)
            }
        }

    // registerCallback(callback, id, rate)는 API 35에서 지원 중단 표시가 붙었지만, 대체 API(subscribePropertyEvents)는 API 35부터라 targetSdk 34에서는 이것을 쓴다
    @Suppress("DEPRECATION")
    private fun carState(): Flow<VehicleState> = callbackFlow {
        val car = try {
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) Car.createCar(context) else null
        } catch (e: Throwable) {
            Log.w(TAG, "차량 서비스에 연결할 수 없어요", e)
            null
        }
        if (car == null) {
            trySend(fallback)
            awaitClose {}
            return@callbackFlow
        }
        val props = car.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
        val ux = car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
        var speed = fallback.speedKmh
        var temperature = fallback.outsideTemperatureCelsius
        var restricted = ux?.currentCarUxRestrictions?.isRequiresDistractionOptimization ?: false

        fun emit() {
            trySend(VehicleState(speed, temperature, restricted))
        }

        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val v = value.value as? Float ?: return
                when (value.propertyId) {
                    VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                        val kmh = abs(v) * 3.6f
                        if (abs(kmh - speed) >= 1f) Log.d(TAG, "속도 $kmh km/h")
                        speed = kmh
                    }
                    VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE -> temperature = v
                }
                emit()
            }

            override fun onErrorEvent(propertyId: Int, zone: Int) {}
        }
        // 권한이 있을 때 한 번만 구독한다. 구독을 시도했으면 true (차에 그 값이 없어도 다시 시도하지 않는다)
        var speedSubscribed = false
        var temperatureSubscribed = false
        fun subscribe() {
            if (props == null) return
            if (!speedSubscribed && granted(Car.PERMISSION_SPEED)) {
                speedSubscribed = true
                val ok = props.registerCallback(callback, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_NORMAL)
                Log.i(TAG, "속도 구독: $ok")
            }
            if (!temperatureSubscribed && granted(Car.PERMISSION_EXTERIOR_ENVIRONMENT)) {
                temperatureSubscribed = true
                // 바깥 온도는 계속 바뀌는 값이라 ONCHANGE(0)가 아니라 기본 빈도로 구독한다
                val ok = props.registerCallback(callback, VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE, CarPropertyManager.SENSOR_RATE_NORMAL)
                Log.i(TAG, "바깥 온도 구독: $ok")
            }
        }
        try {
            subscribe()
            ux?.registerListener { r ->
                restricted = r.isRequiresDistractionOptimization
                emit()
            }
        } catch (e: Exception) {
            Log.w(TAG, "차량 신호를 구독할 수 없어요", e)
        }
        emit()
        // 앱을 처음 켤 때 뜨는 권한 창에서 나중에 허용하면 그때 구독한다
        if (props != null && (!speedSubscribed || !temperatureSubscribed)) {
            launch {
                while (isActive && (!speedSubscribed || !temperatureSubscribed)) {
                    delay(PERMISSION_POLL_MS)
                    try {
                        subscribe()
                    } catch (e: Exception) {
                        Log.w(TAG, "차량 신호를 구독할 수 없어요", e)
                        break
                    }
                }
            }
        }
        awaitClose {
            try {
                props?.unregisterCallback(callback)
                ux?.unregisterListener()
            } catch (_: Exception) {
            }
            car.disconnect()
        }
    }

    private companion object {
        const val TAG = "CarVehicleSignals"
        const val PERMISSION_POLL_MS = 1_000L
        const val DEBUG_SPEED_FILE = "debug_speed.txt"
        const val DEBUG_DRIVING_FILE = "debug_driving.txt"
        const val DEBUG_SPEED_POLL_MS = 1_000L
    }
}
