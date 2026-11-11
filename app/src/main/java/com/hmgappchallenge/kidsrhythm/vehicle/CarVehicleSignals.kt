package com.hmgappchallenge.kidsrhythm.vehicle

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.drivingstate.CarUxRestrictionsManager
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

    // registerCallback(callback, id, rate)는 API 35에서 지원 중단 표시가 붙었지만, 대체 API(subscribePropertyEvents)는 API 35부터라 targetSdk 34에서는 이것을 쓴다
    @Suppress("DEPRECATION")
    override val state: Flow<VehicleState> = callbackFlow {
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
                    VehiclePropertyIds.PERF_VEHICLE_SPEED -> speed = abs(v) * 3.6f
                    VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE -> temperature = v
                }
                emit()
            }

            override fun onErrorEvent(propertyId: Int, zone: Int) {}
        }
        try {
            if (props != null && granted(Car.PERMISSION_SPEED)) {
                props.registerCallback(callback, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_NORMAL)
            }
            if (props != null && granted(Car.PERMISSION_EXTERIOR_ENVIRONMENT)) {
                props.registerCallback(callback, VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE, CarPropertyManager.SENSOR_RATE_ONCHANGE)
            }
            ux?.registerListener { r ->
                restricted = r.isRequiresDistractionOptimization
                emit()
            }
        } catch (e: Exception) {
            Log.w(TAG, "차량 신호를 구독할 수 없어요", e)
        }
        emit()
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
    }
}
