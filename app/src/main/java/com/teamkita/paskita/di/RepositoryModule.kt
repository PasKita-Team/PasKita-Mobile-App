package com.teamkita.paskita.di

import com.teamkita.paskita.data.network.ApiRajaOngkir
import com.teamkita.paskita.data.repositories.DataRepository

object RepositoryModule {

    private var apiRajaOngkirInstance: ApiRajaOngkir? = null

    fun initialize(apiRajaOngkir: ApiRajaOngkir) {
        apiRajaOngkirInstance = apiRajaOngkir
    }

    fun providesDataRepository(): DataRepository {
        if (apiRajaOngkirInstance == null) {
            throw IllegalStateException("ApiRajaOngkir has not been initialized.")
        }

        return DataRepository(apiRajaOngkirInstance!!)
    }
}
