package com.hmgappchallenge.kidsrhythm.vehicle

import kotlinx.coroutines.flow.Flow

data class VehicleState(
    val speedKmh: Float,
    val outsideTemperatureCelsius: Float?,
    val isUxRestricted: Boolean,
)

/** 차량 신호 입력. AAOS Car API와 Pleos Vehicle SDK 중 구현 방식은 미정이다. */
interface VehicleSignals {
    val state: Flow<VehicleState>
}
