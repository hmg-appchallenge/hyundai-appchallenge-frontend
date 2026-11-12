package com.hmgappchallenge.kidsrhythm.vehicle

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** 미리보기·테스트용 고정 값. 시안과 같은 42 km/h로 달리는 상태다. 앱은 [CarVehicleSignals]를 쓴다. */
class FakeVehicleSignals(speedKmh: Float = 42f) : VehicleSignals {
    override val state: Flow<VehicleState> = flowOf(
        VehicleState(speedKmh = speedKmh, outsideTemperatureCelsius = null, isUxRestricted = false),
    )
}
