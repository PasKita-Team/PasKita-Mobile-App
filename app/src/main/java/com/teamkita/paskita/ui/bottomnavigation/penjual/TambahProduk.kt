package com.teamkita.paskita.ui.bottomnavigation.penjual

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.teamkita.paskita.R
import com.teamkita.paskita.data.HasilGenerate
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ActivityTambahProdukBinding
import com.teamkita.paskita.ui.bottomnavigation.penjual.adapter.TemplateAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.KeranjangProdukAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangActivity
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangViewModel
import me.abhinay.input.CurrencySymbols.INDONESIA
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


class TambahProduk : AppCompatActivity() {

    private lateinit var binding: ActivityTambahProdukBinding
    private lateinit var viewModel: TambahProdukViewModel

    private val PERMISSION_CAMERA_REQUEST_CODE = 1
    private val PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 1

    private lateinit var storage: StorageReference
    private lateinit var uri_produk: Uri
    private lateinit var uri_foto_pendukung1: Uri
    private lateinit var uri_foto_pendukung2: Uri
    private lateinit var uri_foto_pendukung3: Uri

    private var url_produk: String? = null
    private var url_foto_1: String? = null
    private var url_foto_2: String? = null
    private var url_foto_3: String? = null
    private var id_produk: String? = null

    private var namaToko: String? = null
    private var alamatToko: String? = null
    private var url_foto_produk:String? = null

    val nUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val collectionRef = db.collection("produk")
        val newDocRef = collectionRef.document()
        id_produk = newDocRef.id

        val userDocument = db.collection("penjual").document(nUser?.uid.toString())
        userDocument.addSnapshotListener { document, _ ->
            if (document != null && document.exists()) {
                namaToko = document.getString("namaToko")
                alamatToko = document.getString("alamatToko")
            }
        }

        storage = FirebaseStorage.getInstance().reference
        cekPermission()

        viewModel = ViewModelProvider(this)[TambahProdukViewModel::class.java]
        viewModel.url_produk.observe(this, Observer { url ->
            if (url.isNotEmpty()){
                url_foto_produk = url
            }else{
                url_foto_produk = url_produk
            }
        })

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvProduk.layoutManager = layoutManager

        setActionButton()
        setupAction()
    }

    private fun setupAction() {

        binding.ivBack.setOnClickListener {
            val intent = Intent(this, DashboardPenjual::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnGenerate.setOnClickListener {
            nUser?.let { user ->
                updateDataGenerate(user)
            }

        }

        binding.cvFotoProduk.setOnClickListener {
            pilihGambarProduk()
        }

        binding.cvFotoPendukung1.setOnClickListener {
            pilihGambarPendukung1()
        }

        binding.cvFotoPendukung2.setOnClickListener {
            pilihGambarPendukung2()
        }

        binding.cvFotoPendukung3.setOnClickListener {
            pilihGambarPendukung3()
        }

        binding.btnSimpan.setOnClickListener {
            nUser?.let { user ->
                simpanData(user)
            }
        }
    }

    private fun setActionButton() {
        val kategori = arrayOf("Kategori Produk", "Makanan", "Minuman", "Kerajinan", "Sambal")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategori)
        binding.spKategori.adapter = adapter

        binding.etHargaProduk.setCurrency(INDONESIA)
        binding.etHargaProduk.setDecimals(false)
        binding.etHargaProduk.setSeparator(".")

        binding.btnGenerate.isEnabled = true
        showLoading(false)
        showTvHasilGenerate(false)
        showTvSilahkanPilih(false)
        showRvProduk(false)
        binding.cbFotoProduk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                binding.btnGenerate.setBackgroundColor(Color.GRAY)
                binding.btnGenerate.isEnabled = false
                showLoading(false)
                showTvHasilGenerate(false)
                showTvSilahkanPilih(false)
                showRvProduk(false)
            }else{
                binding.btnGenerate.isEnabled = true
                binding.btnGenerate.setBackgroundColor(Color.parseColor("#4AB7B6"))
            }
        }

        binding.btnSimpan.setBackgroundColor(Color.GRAY)
        binding.btnSimpan.isEnabled = false
        binding.cbSetuju2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                binding.btnSimpan.isEnabled = true
                binding.btnSimpan.setBackgroundColor(Color.parseColor("#4AB7B6"))
            }else{
                binding.btnSimpan.setBackgroundColor(Color.GRAY)
                binding.btnSimpan.isEnabled = false
            }
        }
    }

    private fun setDataTemplate(template: List<HasilGenerate>) {
        if(template.isNotEmpty()){
            showLoading(false)
            showTvHasilGenerate(true)
            showTvSilahkanPilih(true)
            showRvProduk(true)
            val produkAdapter = TemplateAdapter(this)
            produkAdapter.submitList(template)
            binding.rvProduk.adapter = produkAdapter
        }
    }

    private fun pilihGambarProduk() {
        val items = arrayOf<CharSequence>("Ambil Foto", "Ambil Dari Galeri", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Gambar")
        builder.setIcon(R.drawable.tulisan_paskita)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Ambil Foto" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 10)
                }
                items[item] == "Ambil Dari Galeri" -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), 20)
                }
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun pilihGambarPendukung1() {
        val items = arrayOf<CharSequence>("Ambil Foto", "Ambil Dari Galeri", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Gambar")
        builder.setIcon(R.drawable.tulisan_paskita)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Ambil Foto" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 30)
                }
                items[item] == "Ambil Dari Galeri" -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), 40)
                }
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun pilihGambarPendukung2() {
        val items = arrayOf<CharSequence>("Ambil Foto", "Ambil Dari Galeri", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Gambar")
        builder.setIcon(R.drawable.tulisan_paskita)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Ambil Foto" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 50)
                }
                items[item] == "Ambil Dari Galeri" -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), 60)
                }
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun pilihGambarPendukung3() {
        val items = arrayOf<CharSequence>("Ambil Foto", "Ambil Dari Galeri", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Gambar")
        builder.setIcon(R.drawable.tulisan_paskita)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Ambil Foto" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 70)
                }
                items[item] == "Ambil Dari Galeri" -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), 80)
                }
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun uploadGambarProduk() {
        binding.ivProduk.isDrawingCacheEnabled = true
        binding.ivProduk.buildDrawingCache(true)
        val bitmapDrawable = binding.ivProduk.drawable as BitmapDrawable
        val bitmap: Bitmap = bitmapDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val reference: StorageReference = storage.child("Foto_Produk/produk_${id_produk}.jpg")

        val uploadTask: UploadTask = reference.putBytes(data)
        uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
            progressDialog.dismiss()
            val downloadUrlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    val dowloadUrl: Uri? = task.result
                    if (dowloadUrl != null) {
                        uri_produk = dowloadUrl
                        url_produk = uri_produk.toString()
                    }
                } else {
                    Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
                }
            })
        }).addOnFailureListener(OnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
        }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            progressDialog.setMessage("Uploaded : " + progress.toInt() + "%")
        })
    }

    private fun uploadGambarPendukung1() {
        binding.ivFotoPendukung1.isDrawingCacheEnabled = true
        binding.ivFotoPendukung1.buildDrawingCache(true)
        val bitmapDrawable = binding.ivFotoPendukung1.drawable as BitmapDrawable
        val bitmap: Bitmap = bitmapDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val reference: StorageReference = storage.child("Foto_Produk/foto_pendukung1_${id_produk}.jpg")

        val uploadTask: UploadTask = reference.putBytes(data)
        uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
            progressDialog.dismiss()
            val downloadUrlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    val dowloadUrl: Uri? = task.result
                    if (dowloadUrl != null) {
                        uri_foto_pendukung1 = dowloadUrl
                        url_foto_1 = uri_foto_pendukung1.toString()
                    }
                } else {
                    Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
                }
            })
        }).addOnFailureListener(OnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
        }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            progressDialog.setMessage("Uploaded : " + progress.toInt() + "%")
        })
    }

    private fun uploadGambarPendukung2() {
        binding.ivFotoPendukung2.isDrawingCacheEnabled = true
        binding.ivFotoPendukung2.buildDrawingCache(true)
        val bitmapDrawable = binding.ivFotoPendukung2.drawable as BitmapDrawable
        val bitmap: Bitmap = bitmapDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val reference: StorageReference = storage.child("Foto_Produk/foto_pendukung2_${id_produk}.jpg")

        val uploadTask: UploadTask = reference.putBytes(data)
        uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
            progressDialog.dismiss()
            val downloadUrlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    val dowloadUrl: Uri? = task.result
                    if (dowloadUrl != null) {
                        uri_foto_pendukung2 = dowloadUrl
                        url_foto_2 = uri_foto_pendukung2.toString()
                    }
                } else {
                    Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
                }
            })
        }).addOnFailureListener(OnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
        }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            progressDialog.setMessage("Uploaded : " + progress.toInt() + "%")
        })
    }

    private fun uploadGambarPendukung3() {
        binding.ivFotoPendukung3.isDrawingCacheEnabled = true
        binding.ivFotoPendukung3.buildDrawingCache(true)
        val bitmapDrawable = binding.ivFotoPendukung3.drawable as BitmapDrawable
        val bitmap: Bitmap = bitmapDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val reference: StorageReference = storage.child("Foto_Produk/foto_pendukung3_${id_produk}.jpg")

        val uploadTask: UploadTask = reference.putBytes(data)
        uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
            progressDialog.dismiss()
            val downloadUrlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    val dowloadUrl: Uri? = task.result
                    if (dowloadUrl != null) {
                        uri_foto_pendukung3 = dowloadUrl
                        url_foto_3 = uri_foto_pendukung3.toString()
                    }
                } else {
                    Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
                }
            })
        }).addOnFailureListener(OnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Upload Gagal", Toast.LENGTH_SHORT).show()
        }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            progressDialog.setMessage("Uploaded : " + progress.toInt() + "%")
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 20 && resultCode == RESULT_OK && data != null) {
            val path: Uri? = data.data
            val thread = Thread {
                try {
                    val stream: InputStream? = this.contentResolver.openInputStream(path!!)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    binding.ivProduk.post {
                        nUser?.let { user ->
                            binding.ivProduk.setImageBitmap(bitmap)
                            uploadGambarProduk()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            thread.start()
        }

        if (requestCode == 10 && resultCode == RESULT_OK) {
            val extras: Bundle? = data?.extras
            val thread = Thread {
                val bitmap = extras?.get("data") as Bitmap?
                binding.ivProduk.post {
                    nUser?.let { user ->
                        binding.ivProduk.setImageBitmap(bitmap)
                        uploadGambarProduk()
                    }
                }
            }
            thread.start()
        }

        if (requestCode == 40 && resultCode == RESULT_OK && data != null) {
            val path: Uri? = data.data
            val thread = Thread {
                try {
                    val stream: InputStream? = this.contentResolver.openInputStream(path!!)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    binding.ivFotoPendukung1.post {
                        nUser?.let { user ->
                            binding.ivFotoPendukung1.setImageBitmap(bitmap)
                            uploadGambarPendukung1()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            thread.start()
        }

        if (requestCode == 30 && resultCode == RESULT_OK) {
            val extras: Bundle? = data?.extras
            val thread = Thread {
                val bitmap = extras?.get("data") as Bitmap?
                binding.ivFotoPendukung1.post {
                    nUser?.let { user ->
                        binding.ivFotoPendukung1.setImageBitmap(bitmap)
                        uploadGambarPendukung1()
                    }
                }
            }
            thread.start()
        }

        if (requestCode == 60 && resultCode == RESULT_OK && data != null) {
            val path: Uri? = data.data
            val thread = Thread {
                try {
                    val stream: InputStream? = this.contentResolver.openInputStream(path!!)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    binding.ivFotoPendukung2.post {
                        nUser?.let { user ->
                            binding.ivFotoPendukung2.setImageBitmap(bitmap)
                            uploadGambarPendukung2()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            thread.start()
        }

        if (requestCode == 50 && resultCode == RESULT_OK) {
            val extras: Bundle? = data?.extras
            val thread = Thread {
                val bitmap = extras?.get("data") as Bitmap?
                binding.ivFotoPendukung2.post {
                    nUser?.let { user ->
                        binding.ivFotoPendukung2.setImageBitmap(bitmap)
                        uploadGambarPendukung2()
                    }
                }
            }
            thread.start()
        }

        if (requestCode == 80 && resultCode == RESULT_OK && data != null) {
            val path: Uri? = data.data
            val thread = Thread {
                try {
                    val stream: InputStream? = this.contentResolver.openInputStream(path!!)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    binding.ivFotoPendukung3.post {
                        nUser?.let { user ->
                            binding.ivFotoPendukung3.setImageBitmap(bitmap)
                            uploadGambarPendukung3()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            thread.start()
        }

        if (requestCode == 70 && resultCode == RESULT_OK) {
            val extras: Bundle? = data?.extras
            val thread = Thread {
                val bitmap = extras?.get("data") as Bitmap?
                binding.ivFotoPendukung3.post {
                    nUser?.let { user ->
                        binding.ivFotoPendukung3.setImageBitmap(bitmap)
                        uploadGambarPendukung3()
                    }
                }
            }
            thread.start()
        }
    }

    private fun updateDataGenerate(user: FirebaseUser) {
        val namaProduk = binding.etNamaProduk.text?.toString()
        val hargaProduk = binding.etHargaProduk.text?.toString()
        val daerahProduk = binding.etDaerah.text?.toString()
        val kataPromosi = binding.etKataPromosi.text?.toString()
        val instagram = binding.etInstagram.text?.toString()
        val whatsapp = binding.etWhatsapp.text?.toString()
        val tiktok = binding.etTiktok.text?.toString()
        val user_uid = user.uid

        val updateData = hashMapOf(
            "user_uid" to user_uid,
            "asal_daerah" to daerahProduk,
            "harga_produk" to hargaProduk,
            "instagram" to instagram,
            "tiktok" to tiktok,
            "whatsapp" to whatsapp,
            "kata_promosi" to kataPromosi,
            "nama_produk" to namaProduk,
            "url_foto_produk" to url_produk,
        )

        val produkDocument = db.collection("generate_uid").document("generate_uid")
        produkDocument.update(updateData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Mulai Generate", Toast.LENGTH_SHORT).show()
                showLoading(true)
            }
    }

    private fun simpanData(user: FirebaseUser) {

        val namaProduk = binding.etNamaProduk.text?.toString()
        val hargaProduk = binding.etHargaProduk.text?.toString()
        val deskripsiProduk = binding.etDescProduk.text?.toString()
        val ketegoriProduk = binding.spKategori.selectedItem.toString()
        val daerahProduk = binding.etDaerah.text?.toString()
        val beratProduk = binding.etBerat.text?.toString()
        val kataPromosi = binding.etKataPromosi.text?.toString()
        val instagram = binding.etInstagram.text?.toString()
        val whatsapp = binding.etWhatsapp.text?.toString()
        val tiktok = binding.etTiktok.text?.toString()
        val terjual = 0
        val rating = 0
        val ulasan_produk = "belum ada ulasan"
        if (namaProduk.isNullOrEmpty() ||
            hargaProduk.isNullOrEmpty() ||
            deskripsiProduk.isNullOrEmpty() ||
            ketegoriProduk.isEmpty() ||
            daerahProduk.isNullOrEmpty() ||
            beratProduk.isNullOrEmpty() ||
            kataPromosi.isNullOrEmpty()) {
            Toast.makeText(applicationContext, "Periksa Lagi Data Yang Anda Inputkan Mungkin Masih Ada Data Penting Yang Belum Anda Inputkan:)", Toast.LENGTH_SHORT).show()
        }else{
            val newProduk = Produk(
                id_produk = id_produk,
                nama_produk = namaProduk,
                harga_produk =  hargaProduk,
                deskripsi_produk = deskripsiProduk,
                kategori_produk = ketegoriProduk,
                daerah_produk = daerahProduk,
                berat_produk  = beratProduk,
                url_foto_produk = url_foto_produk,
                kata_promosi = kataPromosi,
                instagram = instagram,
                whatsapp = whatsapp,
                tiktok = tiktok,
                url_foto_pendukung_1 = url_foto_1,
                url_foto_pendukung_2 = url_foto_2,
                url_foto_pendukung_3 = url_foto_3,
                terjual = terjual.toString(),
                ulasan_produk = ulasan_produk,
                rating = rating.toString(),
                uid_penjual = user.uid,
                nama_toko = namaToko,
                alamat_toko = alamatToko,
            )

            //update ke penjual
            val penjualDocument = db.collection("penjual").document(user.uid)
            penjualDocument.get().addOnSuccessListener { document ->
                if (document != null) {
                    val totalProdukSaatIniString = document.getString("total_produk")
                    val totalProdukSaatIni = totalProdukSaatIniString?.toInt() ?: 0
                    val totalProdukBaru = totalProdukSaatIni + 1

                    val updateData = hashMapOf(
                        "total_produk" to totalProdukBaru.toString(),
                    )

                    penjualDocument.update(updateData as Map<String, Any>)
                        .addOnSuccessListener {
                            // Lanjutkan untuk menyimpan data produk
                            val produkDocument = id_produk?.let { db.collection("produk").document(it) }
                            produkDocument?.set(newProduk)?.addOnSuccessListener {
                                Toast.makeText(this, "Produk Berhasil Ditambahkan", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, DashboardPenjual::class.java)
                                startActivity(intent)
                                finish()
                            }?.addOnFailureListener { e ->
                                Log.w("TambahProduk", "failure : ", e)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("TambahProduk", "failure : ", e)
                        }
                } else {
                    Log.d("TambahProduk", "Dokumen penjual tidak ditemukan.")
                }
            }.addOnFailureListener { e ->
                Log.w("TambahProduk", "Gagal mendapatkan dokumen penjual: ", e)
            }

            val produkCollection = db.collection("hasil_generated")
            val query = produkCollection.whereEqualTo("user_id", user.uid)

            query.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val updates = HashMap<String, Any?>()

                        for (field in document.data.keys) {
                            updates[field] = null
                        }

                        document.reference
                            .update(updates)
                            .addOnSuccessListener {
                                // Dokumen berhasil diperbarui dengan field menjadi null

                                // Setelah field-field diubah menjadi null, hapus dokumen
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        // Dokumen berhasil dihapus setelah field-field diubah menjadi null
                                    }
                                    .addOnFailureListener { e ->
                                        // Penanganan kesalahan jika gagal menghapus dokumen
                                        Log.w("Firestore", "Error deleting document", e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                // Penanganan kesalahan jika gagal memperbarui dokumen
                                Log.w("Firestore", "Error updating document", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Penanganan kesalahan jika gagal mendapatkan dokumen
                    Log.w("Firestore", "Error getting documents", e)
                }

        }

    }
    
    private fun cekPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_CAMERA_REQUEST_CODE
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    private fun showLoading(state: Boolean) { binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE }
    private fun showTvHasilGenerate(state: Boolean) { binding.tvHasilGenerate.visibility = if (state) View.VISIBLE else View.GONE }
    private fun showTvSilahkanPilih(state: Boolean) { binding.tvSilahkanPilih.visibility = if (state) View.VISIBLE else View.GONE }
    private fun showRvProduk(state: Boolean) { binding.rvProduk.visibility = if (state) View.VISIBLE else View.GONE }

    override fun onStart() {
        super.onStart()
        viewModel.fetchTemplateList()
        viewModel.getGenerateListLiveData().observe(this) { listTemplate ->
            setDataTemplate(listTemplate)
        }
    }

}