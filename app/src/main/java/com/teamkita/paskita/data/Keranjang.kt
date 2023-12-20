package com.teamkita.paskita.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class Keranjang(
    @get:PropertyName("id_produk") @set:PropertyName("id_produk")
    var id_produk: String? = null,

    @get:PropertyName("keranjang_by") @set:PropertyName("keranjang_by")
    var keranjang_by: String? = null,

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id_produk)
        parcel.writeString(keranjang_by)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Keranjang> {
        override fun createFromParcel(parcel: Parcel): Keranjang {
            return Keranjang(parcel)
        }

        override fun newArray(size: Int): Array<Keranjang?> {
            return arrayOfNulls(size)
        }
    }
}