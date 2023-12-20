package com.teamkita.paskita.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class Produk(
    @get:PropertyName("id_produk") @set:PropertyName("id_produk")
    var id_produk: String? = null,

    @get:PropertyName("nama_produk") @set:PropertyName("nama_produk")
    var nama_produk: String? = null,

    @get:PropertyName("harga_produk") @set:PropertyName("harga_produk")
    var harga_produk: String? = null,

    @get:PropertyName("deskripsi_produk") @set:PropertyName("deskripsi_produk")
    var deskripsi_produk: String? = null,

    @get:PropertyName("kategori_produk") @set:PropertyName("kategori_produk")
    var kategori_produk: String? = null,

    @get:PropertyName("daerah_produk") @set:PropertyName("daerah_produk")
    var daerah_produk: String? = null,

    @get:PropertyName("berat_produk") @set:PropertyName("berat_produk")
    var berat_produk: String? = null,

    @get:PropertyName("url_foto_produk") @set:PropertyName("url_foto_produk")
    var url_foto_produk: String? = null,

    @get:PropertyName("kata_promosi") @set:PropertyName("kata_promosi")
    var kata_promosi: String? = null,

    @get:PropertyName("instagram") @set:PropertyName("instagram")
    var instagram: String? = null,

    @get:PropertyName("whatsapp") @set:PropertyName("whatsapp")
    var whatsapp: String? = null,

    @get:PropertyName("tiktok") @set:PropertyName("tiktok")
    var tiktok: String? = null,

    @get:PropertyName("url_foto_pendukung_1") @set:PropertyName("url_foto_pendukung_1")
    var url_foto_pendukung_1: String? = null,

    @get:PropertyName("url_foto_pendukung_2") @set:PropertyName("url_foto_pendukung_2")
    var url_foto_pendukung_2: String? = null,

    @get:PropertyName("url_foto_pendukung_3") @set:PropertyName("url_foto_pendukung_3")
    var url_foto_pendukung_3: String? = null,

    @get:PropertyName("terjual") @set:PropertyName("terjual")
    var terjual: String? = null,

    @get:PropertyName("ulasan_produk") @set:PropertyName("ulasan_produk")
    var ulasan_produk: String? = null,

    @get:PropertyName("rating") @set:PropertyName("rating")
    var rating: String? = null,

    @get:PropertyName("uid_penjual") @set:PropertyName("uid_penjual")
    var uid_penjual: String? = null,

    @get:PropertyName("nama_toko") @set:PropertyName("nama_toko")
    var nama_toko: String? = null,

    @get:PropertyName("alamat_toko") @set:PropertyName("alamat_toko")
    var alamat_toko: String? = null,

    @get:PropertyName("favorite") @set:PropertyName("favorite")
    var favorite: String? = null,

    @get:PropertyName("favorite_by") @set:PropertyName("favorite_by")
    var favorite_by: String? = null,

    @get:PropertyName("keranjang") @set:PropertyName("keranjang")
    var keranjang: String? = null,

    @get:PropertyName("keranjang_by") @set:PropertyName("keranjang_by")
    var keranjang_by: String? = null,

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id_produk)
        parcel.writeString(nama_produk)
        parcel.writeString(harga_produk)
        parcel.writeString(deskripsi_produk)
        parcel.writeString(kategori_produk)
        parcel.writeString(daerah_produk)
        parcel.writeString(berat_produk)
        parcel.writeString(url_foto_produk)
        parcel.writeString(kata_promosi)
        parcel.writeString(instagram)
        parcel.writeString(whatsapp)
        parcel.writeString(tiktok)
        parcel.writeString(url_foto_pendukung_1)
        parcel.writeString(url_foto_pendukung_2)
        parcel.writeString(url_foto_pendukung_3)
        parcel.writeString(terjual)
        parcel.writeString(ulasan_produk)
        parcel.writeString(rating)
        parcel.writeString(uid_penjual)
        parcel.writeString(nama_toko)
        parcel.writeString(alamat_toko)
        parcel.writeString(favorite)
        parcel.writeString(favorite_by)
        parcel.writeString(keranjang)
        parcel.writeString(keranjang_by)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Produk> {
        override fun createFromParcel(parcel: Parcel): Produk {
            return Produk(parcel)
        }

        override fun newArray(size: Int): Array<Produk?> {
            return arrayOfNulls(size)
        }
    }
}
