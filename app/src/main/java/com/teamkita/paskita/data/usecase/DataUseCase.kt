package com.teamkita.paskita.data.usecase

import com.teamkita.paskita.data.model.ResultData
import com.teamkita.paskita.data.model.city.CityResponse
import com.teamkita.paskita.data.model.cost.CostResponse
import com.teamkita.paskita.data.repositories.DataRepository

class DataUseCase(
    private val dataRepository: DataRepository
) {

    companion object {
        const val STATUS_OK = 200
    }

    suspend fun getCities(): ResultData<CityResponse> {
        return try {
            val cityResponse = dataRepository.getCities()
            if (cityResponse.rajaOngkir?.status?.code == STATUS_OK) {
                ResultData.Success(cityResponse)
            } else {
                ResultData.Failed(cityResponse.rajaOngkir?.status?.description)
            }
        } catch (e: Exception) {
            ResultData.Exception(e.message)
        }
    }

    suspend fun getCost(
        origin: String,
        destination: String,
        weight: Int,
        courier: String
    ): ResultData<CostResponse> {
        return try {
            val costResponse = dataRepository.getCost(origin, destination, weight, courier)
            if (costResponse.rajaOngkir?.status?.code == STATUS_OK) {
                ResultData.Success(costResponse)
            } else {
                ResultData.Failed(costResponse.rajaOngkir?.status?.description)
            }
        } catch (e: Exception) {
            ResultData.Exception(e.message)
        }
    }
}