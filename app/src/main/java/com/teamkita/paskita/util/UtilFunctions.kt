package com.teamkita.paskita.util

import android.util.Log
import java.util.*

fun localID(): Locale {
    return Locale("in", "ID")
}

fun loge(text: String) {
    Log.e("ERROR", "MESSAGE : $text")
}