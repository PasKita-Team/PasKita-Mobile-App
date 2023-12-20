package com.teamkita.paskita.ui.bottomnavigation.user.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.R
import com.teamkita.paskita.data.Favorite
import com.teamkita.paskita.data.Keranjang
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ActivityDetailProdukHomeBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.ProdukAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.notifikasi.NotifikasiActivity
import com.teamkita.paskita.ui.bottomnavigation.user.pesan.PesanActivity
import com.teamkita.paskita.ui.bottomnavigation.user.ratingdanulasan.Rating
import com.teamkita.paskita.ui.bottomnavigation.user.ratingdanulasan.Ulasan
import com.teamkita.paskita.ui.bottomnavigation.user.transaksi.TransaksiActivity
import me.abhinay.input.CurrencySymbols

class DetailProdukHome : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProdukHomeBinding
    private var auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var viewModel: DetailProdukViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProdukHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DetailProdukViewModel::class.java]
        viewModel.currentNumber.observe(this, Observer {
            binding.tvJumlah.text = it.toString()
        })
        setupDataProduk()
        incrementButton()
        decrementButton()

        setupAction()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, BottomNavigation::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun incrementButton(){
        binding.ivTambah.setOnClickListener {
            viewModel.currentNumber.value = ++viewModel.number
        }
    }

    private fun decrementButton(){
        binding.ivKurang.setOnClickListener {
            if (viewModel.number > 1){
                viewModel.currentNumber.value = --viewModel.number
            }
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

        val user = auth.currentUser
        if (user != null){

            binding.ivChat.setOnClickListener {
                val intent = Intent(this, PesanActivity::class.java)
                intent.putExtra("nama_toko", produk.nama_toko)
                intent.putExtra("uid_penjual", produk.uid_penjual)
                startActivity(intent)

                overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
            }

            binding.ivKeranjang.setOnClickListener {
                val produkDocument = db.collection("keranjang").document("${produk.id_produk}_${user.uid}")
                val user = auth.currentUser

                val saveData = Keranjang(
                    id_produk =  produk.id_produk,
                    keranjang_by = user?.uid,
                )
                produkDocument.set(saveData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Di Tambahkan Ke Keranjang", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Produk Adapter", "failure : ", e)
                    }
            }

            binding.ivFav.isChecked = false

            val favCollection = db.collection("favorite").document("${produk.id_produk}_${user.uid}")
            favCollection.get()
                ?.addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userData = documentSnapshot.data
                        val favorite = userData?.get("favorite") as String

                        binding.ivFav.isChecked = favorite.equals("true")
                    }
                }
                ?.addOnFailureListener { e ->
                    Log.w("GetUserData", "Gagal mengambil data: ", e)
                }

            binding.ivFav.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    val produkDocument = db.collection("favorite").document("${produk.id_produk}_${user.uid}")
                    val user = auth.currentUser

                    val saveData = Favorite(
                        id_produk =  produk.id_produk,
                        favorite_by = user?.uid,
                        favorite = "true",
                    )
                    produkDocument.set(saveData)
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener { e ->
                            Log.w("Produk Adapter", "failure : ", e)
                        }
                }else{
                    val produkDocument = db.collection("favorite").document("${produk.id_produk}_${user.uid}")
                    produkDocument.delete()
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Produk Dihapus Dari Favorite", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w("Produk Adapter", "failure : ", e)
                        }
                }
            }
        }else{
            binding.ivKeranjang.setOnClickListener {
                Toast.makeText(applicationContext, "Silahkan Login Terlebih Dahulu", Toast.LENGTH_SHORT).show()
            }
            binding.ivFav.isChecked = false
            binding.ivFav.isEnabled = false
        }

        if (produk.uid_penjual != null && produk.nama_toko != null && produk.alamat_toko != null){
            val namaToko = produk.nama_toko
            val alamatToko = produk.alamat_toko

            "Penjual : $namaToko ($alamatToko)".also { binding.tvNamaPenjual.text = it }
        }

        if (produk.nama_produk != null){
            binding.tvNamaProduk.text = produk.nama_produk
        }

        if (produk.harga_produk != null) {
            val hargaProdukString = produk.harga_produk

            val hargaRegex = Regex("\\d+(\\.\\d+)?")
            val matchResult = hargaRegex.find(hargaProdukString ?: "")

            val harga = matchResult?.value?.toDoubleOrNull() ?: 0.0

            binding.tvHargaProduk.text = hargaProdukString
            binding.tvTotalHarga.setText(hargaProdukString)

            viewModel.currentNumber.observe(this, Observer { number ->
                val numberAsDouble = number.toDouble()
                val totalHarga = numberAsDouble * (harga * 100)
                binding.tvTotalHarga.setCurrency(CurrencySymbols.INDONESIA)
                binding.tvTotalHarga.setDecimals(false)
                binding.tvTotalHarga.setSeparator(".")
                binding.tvTotalHarga.setText(totalHarga.toString())
            })

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
            "Berat Produk : ${produk.berat_produk} gram".also { binding.tvBerat.text = it }
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

        if (produk.ulasan_produk != null){
            if (produk.ulasan_produk.equals("belum ada ulasan")){
                "Belum Ada Ulasan".also { binding.tvRating.text = it }
                binding.ivRating.setImageResource(R.drawable.star_kosong)
            }else{
                "Lihat Ulasan".also { binding.tvRating.text = it }
                binding.ivRating.setImageResource(R.drawable.star_full)
                binding.tvRating.setOnClickListener {
                    val rating = Rating.newInstance(produk)
                    rating.show(supportFragmentManager, Rating.TAG)
                }
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

        binding.btnBeli.setOnClickListener {

            val totalHarga = binding.tvTotalHarga.text.toString()
            val jumlah = binding.tvJumlah.text.toString()

            val intent = Intent(this, TransaksiActivity::class.java)
            intent.putExtra("nama_toko", produk.nama_toko)
            intent.putExtra("nama_produk", produk.nama_produk)
            intent.putExtra("total_harga", totalHarga)
            intent.putExtra("harga", produk.harga_produk)
            intent.putExtra("jumlah", jumlah)
            intent.putExtra("url_foto_produk", produk.url_foto_produk)
            intent.putExtra("alamat_toko", produk.alamat_toko)
            intent.putExtra("produk", produk)
            startActivity(intent)
        }
    }
}