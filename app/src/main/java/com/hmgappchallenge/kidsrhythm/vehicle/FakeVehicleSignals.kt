package com.hmgappchallenge.kidsrhythm.vehicle

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** 차량 신호가 연결되기 전까지 쓰는 고정 값. 시안과 같은 42 km/h로 달리는 상태다. */
// TODO(차량): AAOS Car API 또는 Pleos Vehicle SDK로 실제 속도·주행 제한 상태를 받는다
class FakeVehicleSignals(speedKmh: Float = 42f) : VehicleSignals {
    override val state: Flow<VehicleState> = flowOf(
        VehicleState(speedKmh = speedKmh, outsideTemperatureCelsius = null, isUxRestricted = false),
    )
}
