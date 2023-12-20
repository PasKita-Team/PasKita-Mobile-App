package com.teamkita.paskita.data.model.city


import com.google.gson.annotations.SerializedName

data class CityResponse(
    @SerializedName("rajaongkir")
    val rajaOngkir: CityRajaOngkir? = null
)