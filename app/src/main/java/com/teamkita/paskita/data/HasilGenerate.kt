package com.teamkita.paskita.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class HasilGenerate(
    @get:PropertyName("user_id") @set:PropertyName("user_id")
    var user_id: String? = null,

    @get:PropertyName("image_url") @set:PropertyName("image_url")
    var image_url: String? = null,

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_id)
        parcel.writeString(image_url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HasilGenerate> {
        override fun createFromParcel(parcel: Parcel): HasilGenerate {
            return HasilGenerate(parcel)
        }

        override fun newArray(size: Int): Array<HasilGenerate?> {
            return arrayOfNulls(size)
        }
    }
}