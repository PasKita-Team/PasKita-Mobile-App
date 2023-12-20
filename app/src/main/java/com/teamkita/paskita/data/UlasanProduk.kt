package com.teamkita.paskita.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class UlasanProduk(
    @get:PropertyName("id_produk") @set:PropertyName("id_produk")
    var id_produk: String? = null,

    @get:PropertyName("pembeli") @set:PropertyName("pembeli")
    var pembeli: String? = null,

    @get:PropertyName("ulasan") @set:PropertyName("ulasan")
    var ulasan: String? = null,

    @get:PropertyName("rating") @set:PropertyName("rating")
    var rating: Float = 0f,

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readFloat()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id_produk)
        parcel.writeString(pembeli)
        parcel.writeString(ulasan)
        parcel.writeFloat(rating)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UlasanProduk> {
        override fun createFromParcel(parcel: Parcel): UlasanProduk {
            return UlasanProduk(parcel)
        }

        override fun newArray(size: Int): Array<UlasanProduk?> {
            return arrayOfNulls(size)
        }
    }
}