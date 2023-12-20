package com.teamkita.paskita.data.model.cost


import com.google.gson.annotations.SerializedName

data class CostResponse(
    @SerializedName("rajaongkir")
    val rajaOngkir: CostRajaOngkir? = null
)