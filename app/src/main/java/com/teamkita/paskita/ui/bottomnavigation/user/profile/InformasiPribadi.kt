package com.teamkita.paskita.ui.bottomnavigation.user.profile

import android.R
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.teamkita.paskita.databinding.ActivityInformasiPribadiBinding
import com.teamkita.paskita.ui.bottomnavigation.OnDialogCloseListener
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class InformasiPribadi : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "InformasiPribadi"

        fun newInstance(): InformasiPribadi {
            return InformasiPribadi()
        }
    }

    private lateinit var binding: ActivityInformasiPribadiBinding

    private val PERMISSION_CAMERA_REQUEST_CODE = 1
    private val PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 1

    private lateinit var storage: StorageReference
    private lateinit var uri: Uri

    private var url_profile: String? = null

    private var sebagai: String? = null

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityInformasiPribadiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = FirebaseStorage.getInstance().reference

        cekPermission()

        currentUser?.let { user ->
            val userDocument = db.collection("users").document(user.uid)
            userDocument.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Ambil data dari dokumen Firestore
                    val email = document.getString("email")
                    val nama = document.getString("nama")
                    val jenis_kelamin = document.getString("jenis_kelamin")
                    val no_hp = document.getString("no_hp")
                    val domisili = document.getString("domisili")
                    url_profile = document.getString("url_profile")
                    sebagai = document.getString("as")

                    // Tampilkan data di dalam formulir jika ada
                    if (url_profile != null){
                        Glide.with(requireContext())
                            .load(url_profile)
                            .placeholder(com.teamkita.paskita.R.drawable.tulisan_paskita)
                            .error(com.teamkita.paskita.R.drawable.baseline_error_24)
                            .into(binding.ivImage)
                    }
                    if (nama != null){
                        binding.etNama.setText(nama)
                    }else{
                        binding.etNama.setText(email?.substringBefore('@'))
                    }

                    binding.etNoHp.setText(no_hp)
                    binding.etDomisili.setText(domisili)
                    if (jenis_kelamin != null) {
                        val genderOptions = arrayOf("Jenis Kelamin", "Laki-laki", "Perempuan")
                        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_dropdown_item, genderOptions)
                        binding.spJenisKelamin.adapter = adapter
                        val genderIndex = genderOptions.indexOf(jenis_kelamin)
                        binding.spJenisKelamin.setSelection(genderIndex)
                    }
                }else{
                    binding.etNama.setText(user.displayName)

                    Glide.with(requireContext())
                        .load(user.photoUrl)
                        .placeholder(com.teamkita.paskita.R.drawable.tulisan_paskita)
                        .error(com.teamkita.paskita.R.drawable.baseline_error_24)
                        .into(binding.ivImage)

                    val genderOptions = arrayOf("Jenis Kelamin", "Laki-laki", "Perempuan")
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_dropdown_item, genderOptions)
                    binding.spJenisKelamin.adapter = adapter
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
            pilihGambar()
        }

        binding.btnSimpan.setOnClickListener {
            val nUser = FirebaseAuth.getInstance().currentUser
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
                        currentUser?.let { user ->
                            binding.ivImage.setImageBitmap(bitmap)
                            uploadGambar(user)
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
                    currentUser?.let { user ->
                        binding.ivImage.setImageBitmap(bitmap)
                        uploadGambar(user)
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

    private fun pilihGambar() {
        val items = arrayOf<CharSequence>("Ambil Foto", "Ambil Dari Galeri", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pilih Gambar")
        builder.setIcon(R.drawable.ic_menu_camera)
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

    private fun uploadGambar(user: FirebaseUser) {
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

        val reference: StorageReference = storage.child("Profile/profile"+ " (" + user.uid + ").jpg")
        val uploadTask: UploadTask = reference.putBytes(data)
        uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
            progressDialog.dismiss()
            val downloadUrlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    val dowloadUrl: Uri? = task.result
                    if (dowloadUrl != null) {
                        uri = dowloadUrl
                        url_profile = uri.toString()
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

    private fun simpanData(user: FirebaseUser) {

        // Get updated values from the form fields
        val email = user.email
        val nama = binding.etNama.text?.toString()
        val jenisKelamin = binding.spJenisKelamin.selectedItem.toString()
        val noHp = binding.etNoHp.text?.toString()
        val domisili = binding.etDomisili.text?.toString()

        val saveData = hashMapOf(
            "email" to email,
            "nama" to nama,
            "jenis_kelamin" to jenisKelamin,
            "no_hp" to noHp,
            "domisili" to domisili,
            "url_profile" to url_profile,
            "as" to sebagai
        )
        val userDocument = db.collection("users").document(user.uid)
        userDocument.set(saveData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Update Berhasil", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { e ->
                Log.w("Informasi Pribadi", "failure : ", e)
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