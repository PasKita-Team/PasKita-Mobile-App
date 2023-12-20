package com.teamkita.paskita.ui.bottomnavigation.penjual

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ActivityDetailProdukPenjualBinding

class DetailProdukPenjual : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProdukPenjualBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProdukPenjualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDataProduk()
        setupAction()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, DashboardPenjual::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setImageSlider(
        urlFotoProduk: String?,
        urlFotoPendukung1: String?,
        urlFotoPendukung2: String?,
        urlFotoPendukung3: String?
    ) {
        val imageList = ArrayList<SlideModel>()

        if (urlFotoProduk != null){
            imageList.add(SlideModel(urlFotoProduk))
        }

        if (urlFotoPendukung1 != null){
            imageList.add(SlideModel(urlFotoPendukung1))
        }

        if (urlFotoPendukung2 != null){
            imageList.add(SlideModel(urlFotoPendukung2))
        }

        if (urlFotoPendukung3 != null){
            imageList.add(SlideModel(urlFotoPendukung3))
        }

        binding.imageSlider.setImageList(imageList, ScaleTypes.FIT)
    }

    private fun setupDataProduk() {

        val produk = intent.getParcelableExtra<Produk>("produk") as Produk

        if (produk.uid_penjual != null && produk.nama_toko != null && produk.alamat_toko != null){
            val namaToko = produk.nama_toko
            val alamatToko = produk.alamat_toko

            "Penjual : $namaToko ($alamatToko)".also { binding.tvNamaPenjual.text = it }
        }

        if (produk.nama_produk != null){
            binding.tvNamaProduk.text = produk.nama_produk
        }

        if (produk.harga_produk != null){
            binding.tvHargaProduk.text = produk.harga_produk
        }

        if (produk.deskripsi_produk != null){
            binding.tvDeskripsi.text = produk.deskripsi_produk
        }

        if (produk.kategori_produk != null){
            "Kategori : ${produk.kategori_produk}".also { binding.tvKategori.text = it }
        }

        if (produk.daerah_produk != null){
            "Asal Daerah : ${produk.daerah_produk}".also { binding.tvDaerah.text = it }
        }

        if (produk.berat_produk != null){
            "Berat Produk : ${produk.berat_produk}".also { binding.tvPengiriman.text = it }
        }

        if (produk.instagram.equals("")){
            binding.tvInstagram.visibility = View.GONE
        }else{
            binding.tvInstagram.text = produk.instagram
        }

        if (produk.whatsapp.equals("")){
            binding.tvWhatsapp.visibility = View.GONE
        }else{
            binding.tvWhatsapp.text = produk.whatsapp
        }

        if (produk.tiktok.equals("")){
            binding.tvTiktok.visibility = View.GONE
        }else{
            binding.tvTiktok.text = produk.tiktok
        }

        if (produk.rating != null){
            if (produk.rating.equals("0")){
                "Belum Ada Ulasan".also { binding.tvRating.text = it }
            }else{
                binding.tvRating.text = produk.rating
            }
        }

        if (produk.terjual != null){
            "| ${produk.terjual} Terjual".also { binding.tvTerjual.text = it }
        }


        if (produk.url_foto_produk != null
            || produk.url_foto_pendukung_1 != null
            || produk.url_foto_pendukung_2 != null
            || produk.url_foto_pendukung_3 != null){
            setImageSlider(produk.url_foto_produk, produk.url_foto_pendukung_1, produk.url_foto_pendukung_2, produk.url_foto_pendukung_3)
        }


    }
}