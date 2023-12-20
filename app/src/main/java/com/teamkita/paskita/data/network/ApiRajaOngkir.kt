package com.teamkita.paskita.data.network

import com.teamkita.paskita.data.model.city.CityResponse
import com.teamkita.paskita.data.model.cost.CostResponse
import retrofit2.http.*

interface ApiRajaOngkir {

    @GET("city")
    suspend fun getCities(
        @Query("key") apiKey: String?
    ): CityResponse

    @FormUrlEncoded
    @POST("cost")
    suspend fun getCost(
        @Header("key") apiKey: String?,
        @Field("origin") origin: String?,
        @Field("destination") destination: String?,
        @Field("weight") weight: Int?,
        @Field("courier") courier: String?
    ): CostResponse
}