package com.teamkita.paskita.ui.bottomnavigation.penjual

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.teamkita.paskita.R
import com.teamkita.paskita.databinding.ActivityDaftarPenjualBinding
import com.teamkita.paskita.ui.bottomnavigation.OnDialogCloseListener
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class DaftarPenjual : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "DaftarPenjual"

        fun newInstance(): DaftarPenjual {
            return DaftarPenjual()
        }
    }

    private lateinit var binding: ActivityDaftarPenjualBinding

    private val PERMISSION_CAMERA_REQUEST_CODE = 1
    private val PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 1

    private lateinit var storage: StorageReference
    private lateinit var uri_profile: Uri
    private lateinit var uri_berkas: Uri

    private var url_profile_toko: String? = null
    private var url_verifikasi_berkas: String? = null

    val nUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityDaftarPenjualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = FirebaseStorage.getInstance().reference
        cekPermission()

        nUser?.let { user ->
            val userDocument = db.collection("penjual").document(user.uid)
            userDocument.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {

                    "Edit Profile Toko".also { binding.tvJudul.text = it }
                    binding.cbDaftar.visibility = View.GONE
                    binding.cbSetuju.visibility = View.GONE
                    binding.btnSimpan.visibility = View.GONE
                    binding.btnUpdate.visibility = View.VISIBLE

                    // Ambil data dari dokumen Firestore
                    url_profile_toko = document.getString("url_profile_toko")
                    val namaToko = document.getString("namaToko")
                    val alamatToko = document.getString("alamatToko")
                    val deskripsiToko = document.getString("deskripsiToko")
                    url_verifikasi_berkas = document.getString("url_verifikasi_berkas")
                    val namaBank = document.getString("namaBank")
                    val noRek = document.getString("noRek")

                    binding.etNamaToko.setText(namaToko)
                    binding.etAlamatToko.setText(alamatToko)
                    binding.etDesc.setText(deskripsiToko)
                    binding.etNamaBank.setText(namaBank)
                    binding.etNoRek.setText(noRek)

                    if (url_profile_toko != null){
                        Glide.with(requireContext())
                            .load(url_profile_toko)
                            .placeholder(R.drawable.tulisan_paskita)
                            .error(R.drawable.baseline_error_24)
                            .into(binding.ivImage)
                    }

                    if (url_verifikasi_berkas != null){
                        Glide.with(requireContext())
                            .load(url_verifikasi_berkas)
                            .placeholder(R.drawable.tulisan_paskita)
                            .error(R.drawable.baseline_error_24)
                            .into(binding.ivKtp)
                    }

                }else{
                    "Daftar Menjadi Penjual".also { binding.tvJudul.text = it }
                    binding.cbDaftar.visibility = View.VISIBLE
                    binding.cbSetuju.visibility = View.VISIBLE

                    binding.btnSimpan.visibility = View.VISIBLE
                    binding.btnUpdate.visibility = View.GONE

                    binding.btnSimpan.setBackgroundColor(Color.GRAY)
                    binding.btnSimpan.isEnabled = false

                    binding.cbSetuju.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked){
                            binding.cbDaftar.setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked){
                                    binding.btnSimpan.isEnabled = true
                                    binding.btnSimpan.setBackgroundColor(Color.parseColor("#4AB7B6"))
                                }else{
                                    binding.btnSimpan.setBackgroundColor(Color.GRAY)
                                    binding.btnSimpan.isEnabled = false
                                }
                            }
                        }else{
                            binding.btnSimpan.setBackgroundColor(Color.GRAY)
                            binding.btnSimpan.isEnabled = false
                        }
                    }


                }
            }
        }

        setupAction()

    }

    private fun setupAction() {

        binding.ivBack.setOnClickListener {
            dismiss()
        }

        binding.cvFotoProfile.setOnClickListener {
            pilihGambarProfile()
        }

        binding.cvFotoKTP.setOnClickListener {
            pilihGambarBerkas()
        }

        binding.btnUpdate.setOnClickListener {
            nUser?.let { user ->
                updateData(user)
            }
        }

        binding.btnSimpan.setOnClickListener {
            nUser?.let { user ->
                simpanData(user)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 20 && resultCode == RESULT_OK && data != null) {
            val path: Uri? = data.data
            val thread = Thread {
                try {
                    val stream: InputStream? = requireActivity().contentResolver.openInputStream(path!!)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    binding.ivImage.post {
                        nUser?.let { user ->
                            binding.ivImage.setImageBitmap(bitmap)
                            uploadGambarProfile(user)
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
                binding.ivImage.post {
                    nUser?.let { user ->
                        binding.ivImage.setImageBitmap(bitmap)
                        uploadGambarProfile(user)
                    }
                }
            }
            thread.start()
        }

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            val path: Uri? = data.data
            val thread = Thread {
                try {
                    val stream: InputStream? = requireActivity().contentResolver.openInputStream(path!!)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    binding.ivKtp.post {
                        nUser?.let { user ->
                            binding.ivKtp.setImageBitmap(bitmap)
                            uploadGambarBerkas(user)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            thread.start()
        }

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val extras: Bundle? = data?.extras
            val thread = Thread {
                val bitmap = extras?.get("data") as Bitmap?
                binding.ivKtp.post {
                    nUser?.let { user ->
                        binding.ivKtp.setImageBitmap(bitmap)
                        uploadGambarBerkas(user)
                    }
                }
            }
            thread.start()
        }
    }

    private fun cekPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_CAMERA_REQUEST_CODE
            )
        }

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    private fun pilihGambarProfile() {
        val items = arrayOf<CharSequence>("Ambil Foto", "Ambil Dari Galeri", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
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

    private fun pilihGambarBerkas() {
        val items = arrayOf<CharSequence>("Ambil Foto", "Ambil Dari Galeri", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pilih Gambar")
        builder.setIcon(R.drawable.tulisan_paskita)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Ambil Foto" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 100)
                }
                items[item] == "Ambil Dari Galeri" -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), 200)
                }
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun uploadGambarProfile(user: FirebaseUser) {
        binding.ivImage.isDrawingCacheEnabled = true
        binding.ivImage.buildDrawingCache(true)
        val bitmapDrawable = binding.ivImage.drawable as BitmapDrawable
        val bitmap: Bitmap = bitmapDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val reference: StorageReference = storage.child("Penjual/profile"+ " (" + user.uid + ").jpg")
        val uploadTask: UploadTask = reference.putBytes(data)
        uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
            progressDialog.dismiss()
            val downloadUrlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    val dowloadUrl: Uri? = task.result
                    if (dowloadUrl != null) {
                        uri_profile = dowloadUrl
                        url_profile_toko = uri_profile.toString()
                    }
                } else {
                    Toast.makeText(requireContext(), "Upload Gagal", Toast.LENGTH_SHORT).show()
                }
            })
        }).addOnFailureListener(OnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(requireContext(), "Upload Gagal", Toast.LENGTH_SHORT).show()
        }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            progressDialog.setMessage("Uploaded : " + progress.toInt() + "%")
        })
    }
    private fun uploadGambarBerkas(user: FirebaseUser) {
        binding.ivKtp.isDrawingCacheEnabled = true
        binding.ivKtp.buildDrawingCache(true)
        val bitmapDrawable = binding.ivKtp.drawable as BitmapDrawable
        val bitmap: Bitmap = bitmapDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val reference: StorageReference = storage.child("Penjual/berkas"+ " (" + user.uid + ").jpg")
        val uploadTask: UploadTask = reference.putBytes(data)
        uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
            progressDialog.dismiss()
            val downloadUrlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    val dowloadUrl: Uri? = task.result
                    if (dowloadUrl != null) {
                        uri_berkas = dowloadUrl
                        url_verifikasi_berkas = uri_berkas.toString()
                    }
                } else {
                    Toast.makeText(requireContext(), "Upload Gagal", Toast.LENGTH_SHORT).show()
                }
            })
        }).addOnFailureListener(OnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(requireContext(), "Upload Gagal", Toast.LENGTH_SHORT).show()
        }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
            progressDialog.setMessage("Uploaded : " + progress.toInt() + "%")
        })
    }

    private fun updateData(user: FirebaseUser) {

        val namaToko = binding.etNamaToko.text?.toString()
        val alamatToko = binding.etAlamatToko.text?.toString()
        val deskripsiToko = binding.etDesc.text?.toString()
        val namaBank = binding.etNamaBank.text?.toString()
        val noRek = binding.etNoRek.text?.toString()

        val updateData = hashMapOf(
            "url_profile_toko" to url_profile_toko,
            "namaToko" to namaToko,
            "alamatToko" to alamatToko,
            "deskripsiToko" to deskripsiToko,
            "url_verifikasi_berkas" to url_verifikasi_berkas,
            "namaBank" to namaBank,
            "noRek" to noRek,
        )

        //save ke penjual
        val penjualDocument = db.collection("penjual").document(user.uid)
        penjualDocument.update(updateData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Update Berhasil", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "failure : ", e)
            }
    }

    private fun simpanData(user: FirebaseUser) {

        val namaToko = binding.etNamaToko.text?.toString()
        val alamatToko = binding.etAlamatToko.text?.toString()
        val deskripsiToko = binding.etDesc.text?.toString()
        val namaBank = binding.etNamaBank.text?.toString()
        val noRek = binding.etNoRek.text?.toString()
        val total_produk = 0
        val terjual = 0

        val saveData = hashMapOf(
            "url_profile_toko" to url_profile_toko,
            "namaToko" to namaToko,
            "alamatToko" to alamatToko,
            "deskripsiToko" to deskripsiToko,
            "url_verifikasi_berkas" to url_verifikasi_berkas,
            "namaBank" to namaBank,
            "noRek" to noRek,
            "total_produk" to total_produk.toString(),
            "terjual" to terjual.toString(),
        )

        val updateData = hashMapOf(
            "as" to "penjual",
        )

        //save ke penjual
        val penjualDocument = db.collection("penjual").document(user.uid)
        penjualDocument.set(saveData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Daftar Berhasil", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "failure : ", e)
            }

        //update ke users
        val userDocument = db.collection("users").document(user.uid)
        userDocument.update(updateData as Map<String, Any>)
            .addOnSuccessListener {
                dismiss()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "failure : ", e)
            }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity = requireActivity()
        if (activity is OnDialogCloseListener) {
            (activity as OnDialogCloseListener).onDialogClose(dialog)
        }
    }

}