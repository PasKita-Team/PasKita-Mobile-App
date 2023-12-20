package com.teamkita.paskita.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CostPostageFee(
    val code: String? = null,
    val name: String? = null,
    val service: String? = null,
    val description: String? = null,
    val etd: String? = null,
    val value: Int? = null
) : Parcelable