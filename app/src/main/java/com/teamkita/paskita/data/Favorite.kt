package com.teamkita.paskita.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class Favorite(
    @get:PropertyName("id_produk") @set:PropertyName("id_produk")
    var id_produk: String? = null,

    @get:PropertyName("favorite") @set:PropertyName("favorite")
    var favorite: String? = null,

    @get:PropertyName("favorite_by") @set:PropertyName("favorite_by")
    var favorite_by: String? = null,

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id_produk)
        parcel.writeString(favorite)
        parcel.writeString(favorite_by)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Favorite> {
        override fun createFromParcel(parcel: Parcel): Favorite {
            return Favorite(parcel)
        }

        override fun newArray(size: Int): Array<Favorite?> {
            return arrayOfNulls(size)
        }
    }
}