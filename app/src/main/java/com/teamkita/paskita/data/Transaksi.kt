package com.teamkita.paskita.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName

data class Transaksi(
    @get:PropertyName("id_produk") @set:PropertyName("id_produk")
    var id_produk: String? = null,

    @get:PropertyName("alamatPengiriman") @set:PropertyName("alamatPengiriman")
    var alamatPengiriman: String? = null,

    @get:PropertyName("buktiPembayaran") @set:PropertyName("buktiPembayaran")
    var buktiPembayaran: String? = null,

    @get:PropertyName("catatanProduk") @set:PropertyName("catatanProduk")
    var catatanProduk: String? = null,

    @get:PropertyName("jumlahProduk") @set:PropertyName("jumlahProduk")
    var jumlahProduk: String? = null,

    @get:PropertyName("kota") @set:PropertyName("kota")
    var kota: String? = null,

    @get:PropertyName("kurir") @set:PropertyName("kurir")
    var kurir: String? = null,

    @get:PropertyName("namaPembeli") @set:PropertyName("namaPembeli")
    var namaPembeli: String? = null,

    @get:PropertyName("url_foto_produk") @set:PropertyName("url_foto_produk")
    var url_foto_produk: String? = null,

    @get:PropertyName("namaProduk") @set:PropertyName("namaProduk")
    var namaProduk: String? = null,

    @get:PropertyName("namaToko") @set:PropertyName("namaToko")
    var namaToko: String? = null,

    @get:PropertyName("penjual") @set:PropertyName("penjual")
    var penjual: String? = null,

    @get:PropertyName("provinsi") @set:PropertyName("provinsi")
    var provinsi: String? = null,

    @get:PropertyName("status") @set:PropertyName("status")
    var status: String? = null,

    @get:PropertyName("tanggal") @set:PropertyName("tanggal")
    var tanggal: String? = null,

    @get:PropertyName("totalBayar") @set:PropertyName("totalBayar")
    var totalBayar: String? = null,

    @get:PropertyName("uid_penjual") @set:PropertyName("uid_penjual")
    var uid_penjual: String? = null,

    @get:PropertyName("uid_pembeli") @set:PropertyName("uid_pembeli")
    var uid_pembeli: String? = null,

    @get:PropertyName("no_resi") @set:PropertyName("no_resi")
    var no_resi: String? = null,

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
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id_produk)
        parcel.writeString(alamatPengiriman)
        parcel.writeString(buktiPembayaran)
        parcel.writeString(catatanProduk)
        parcel.writeString(jumlahProduk)
        parcel.writeString(kota)
        parcel.writeString(kurir)
        parcel.writeString(namaPembeli)
        parcel.writeString(url_foto_produk)
        parcel.writeString(namaProduk)
        parcel.writeString(namaToko)
        parcel.writeString(penjual)
        parcel.writeString(provinsi)
        parcel.writeString(status)
        parcel.writeString(tanggal)
        parcel.writeString(totalBayar)
        parcel.writeString(uid_penjual)
        parcel.writeString(uid_pembeli)
        parcel.writeString(no_resi)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaksi> {
        override fun createFromParcel(parcel: Parcel): Transaksi {
            return Transaksi(parcel)
        }

        override fun newArray(size: Int): Array<Transaksi?> {
            return arrayOfNulls(size)
        }
    }
}