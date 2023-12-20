package com.teamkita.paskita.ui.bottomnavigation.user.transaksi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.teamkita.paskita.data.model.ResultData
import com.teamkita.paskita.data.model.city.CityResponse
import com.teamkita.paskita.data.model.cost.CostResponse
import com.teamkita.paskita.data.network.ApiRajaOngkir
import com.teamkita.paskita.data.repositories.DataRepository
import com.teamkita.paskita.data.usecase.DataUseCase
import com.teamkita.paskita.di.NetworkModule

class TransaksiViewModel : ViewModel() {

    val apiRajaOngkir: ApiRajaOngkir = NetworkModule.providesAPIService()
    private val dataRepository = DataRepository(apiRajaOngkir)
    private val dataUseCase = DataUseCase(dataRepository)

    private val selectedValue = MutableLiveData<String>()

    fun setSelectedValue(value: String) {
        selectedValue.value = value
    }

    fun getSelectedValue(): LiveData<String> {
        return selectedValue
    }

    var number: Int = 1

    val currentNumber: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun getCities(): LiveData<ResultData<CityResponse>> {
        return liveData {
            emit(ResultData.Loading())
            emit(dataUseCase.getCities())
        }
    }

    fun getCost(
        origin: String,
        destination: String,
        weight: Int,
        courier: String
    ): LiveData<ResultData<CostResponse>> {
        return liveData {
            emit(ResultData.Loading())
            emit(dataUseCase.getCost(origin, destination, weight, courier))
        }
    }
}
