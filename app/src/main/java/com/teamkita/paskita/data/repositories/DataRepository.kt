package com.teamkita.paskita.data.repositories

import com.teamkita.paskita.data.APIKey.Companion.API_KEY
import com.teamkita.paskita.data.model.city.CityResponse
import com.teamkita.paskita.data.model.cost.CostResponse
import com.teamkita.paskita.data.network.ApiRajaOngkir

class DataRepository(private val apiRajaOngkir: ApiRajaOngkir) {

    suspend fun getCities(): CityResponse {
        return apiRajaOngkir.getCities(API_KEY)
    }

    suspend fun getCost(
        origin: String,
        destination: String,
        weight: Int,
        courier: String
    ): CostResponse {
        return apiRajaOngkir.getCost(API_KEY, origin, destination, weight, courier)
    }
}
