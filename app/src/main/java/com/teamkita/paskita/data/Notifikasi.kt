package com.teamkita.paskita.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class Notifikasi(
    @get:PropertyName("nama_notif") @set:PropertyName("nama_notif")
    var nama_notif: String? = null,

    @get:PropertyName("pesan") @set:PropertyName("pesan")
    var pesan: String? = null,

    @get:PropertyName("uid_user") @set:PropertyName("uid_user")
    var uid_user: String? = null,
)