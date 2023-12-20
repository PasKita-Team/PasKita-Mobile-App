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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
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
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ActivityEditProdukBinding
import me.abhinay.input.CurrencySymbols.INDONESIA
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class EditProduk : AppCompatActivity() {

    private lateinit var binding: ActivityEditProdukBinding
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

    private var terjual: String? = null

    val nUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance().reference
        cekPermission()

        setupDataProduk()
        setActionButton()
        setupAction()
    }
    private fun setupAction() {

        binding.ivBack.setOnClickListener {
            val intent = Intent(this, DashboardPenjual::class.java)
            startActivity(intent)
            finish()
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
                updateData(user)
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
        binding.cbSetuju.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                binding.btnSimpan.isEnabled = true
                binding.btnSimpan.setBackgroundColor(Color.parseColor("#4AB7B6"))
            }else{
                binding.btnSimpan.setBackgroundColor(Color.GRAY)
                binding.btnSimpan.isEnabled = false
            }
        }
    }

    private fun setupDataProduk() {

        val produk = intent.getParcelableExtra<Produk>("produk") as Produk

        binding.cbFotoProduk.isChecked = true
        binding.btnGenerate.setBackgroundColor(Color.GRAY)
        binding.btnGenerate.isEnabled = false

        id_produk = produk.id_produk

        if (produk.nama_produk != null){
            binding.etNamaProduk.setText(produk.nama_produk)
        }

        if (produk.harga_produk != null){
            binding.etHargaProduk.setText(produk.harga_produk)
        }

        if (produk.deskripsi_produk != null){
            binding.etDescProduk.setText(produk.deskripsi_produk)
        }

        if (produk.kategori_produk != null){
            val kategori = arrayOf("Kategori Produk", "Makanan", "Minuman", "Kerajinan", "Sambal")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategori)
            binding.spKategori.adapter = adapter
            val kategoriIndex = kategori.indexOf(produk.kategori_produk)
            binding.spKategori.setSelection(kategoriIndex)
        }

        if (produk.daerah_produk != null){
            binding.etDaerah.setText(produk.daerah_produk)
        }

        if (produk.berat_produk != null){
            binding.etBerat.setText(produk.berat_produk)
        }

        if (produk.url_foto_produk != null){
            url_produk = produk.url_foto_produk
            Glide.with(applicationContext)
                .load(produk.url_foto_produk)
                .into(binding.ivProduk)
        }

        if (produk.kata_promosi != null){
            binding.etKataPromosi.setText(produk.kata_promosi)
        }

        if (produk.instagram != null){
            binding.etInstagram.setText(produk.instagram)
        }

        if (produk.whatsapp != null){
            binding.etWhatsapp.setText(produk.whatsapp)
        }

        if (produk.tiktok != null){
            binding.etTiktok.setText(produk.tiktok)
        }

        if (produk.url_foto_pendukung_1 != null){
            url_foto_1 = produk.url_foto_pendukung_1
            Glide.with(applicationContext)
                .load(produk.url_foto_pendukung_1)
                .into(binding.ivFotoPendukung1)
        }

        if (produk.url_foto_pendukung_2 != null){
            url_foto_2 = produk.url_foto_pendukung_2
            Glide.with(applicationContext)
                .load(produk.url_foto_pendukung_2)
                .into(binding.ivFotoPendukung2)
        }

        if (produk.url_foto_pendukung_3 != null){
            url_foto_3 = produk.url_foto_pendukung_3
            Glide.with(applicationContext)
                .load(produk.url_foto_pendukung_3)
                .into(binding.ivFotoPendukung3)
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

    private fun updateData(user: FirebaseUser) {

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

        if (namaProduk.isNullOrEmpty() ||
            hargaProduk.isNullOrEmpty() ||
            deskripsiProduk.isNullOrEmpty() ||
            ketegoriProduk.isEmpty() ||
            daerahProduk.isNullOrEmpty() ||
            beratProduk.isNullOrEmpty() ||
            kataPromosi.isNullOrEmpty()) {
            Toast.makeText(applicationContext, "Periksa Lagi Data Yang Anda Inputkan Mungkin Masih Ada Data Penting Yang Belum Anda Inputkan:)", Toast.LENGTH_SHORT).show()
        }else{
            val editData = hashMapOf(
                "id_produk" to id_produk,
                "nama_produk" to namaProduk,
                "harga_produk" to  hargaProduk,
                "deskripsi_produk" to deskripsiProduk,
                "kategori_produk" to ketegoriProduk,
                "daerah_produk" to daerahProduk,
                "berat_produk" to beratProduk,
                "url_foto_produk" to url_produk,
                "kata_promosi" to kataPromosi,
                "instagram" to instagram,
                "whatsapp" to whatsapp,
                "tiktok" to tiktok,
                "url_foto_pendukung_1" to url_foto_1,
                "url_foto_pendukung_2" to url_foto_2,
                "url_foto_pendukung_3" to url_foto_3,
            )

            //update ke produk
            val produkDocument = db.collection("produk").document(id_produk.toString())
            produkDocument.update(editData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Produk Berhasil DiUbah", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DashboardPenjual::class.java)
                    startActivity(intent)
                    finish()
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
}