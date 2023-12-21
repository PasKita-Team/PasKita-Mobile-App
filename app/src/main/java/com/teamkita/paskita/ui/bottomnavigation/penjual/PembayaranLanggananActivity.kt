package com.teamkita.paskita.ui.bottomnavigation.penjual

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
import com.teamkita.paskita.data.Notifikasi
import com.teamkita.paskita.databinding.ActivityPembayaranLanggananBinding
import com.teamkita.paskita.ui.bottomnavigation.user.notifikasi.NotifikasiActivity
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PembayaranLanggananActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPembayaranLanggananBinding

    private val PERMISSION_CAMERA_REQUEST_CODE = 1
    private val PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 1

    private lateinit var storage: StorageReference
    private lateinit var uri_pembayaran: Uri
    private var url_pembayaran: String? = null
    val nUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications permission rejected", Toast.LENGTH_SHORT).show()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPembayaranLanggananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val harga= intent.getStringExtra("harga")
        binding.tvTotalBayar.text = harga
        binding.tvTanggal.text = getCurrentDate()

        storage = FirebaseStorage.getInstance().reference
        cekPermission()
        setupAction()
        
    }

    private fun setupAction() {
        
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, TambahProduk::class.java)
            startActivity(intent)
            finish()
        }
        
        binding.ivPembayaran.setOnClickListener {
            pilihFoto()
        }

        binding.btnKirim.setOnClickListener {
            nUser?.let { user ->
                simpanData(user)
            }
        }

    }

    private fun simpanData(user: FirebaseUser) {
        val harga= intent.getStringExtra("harga")
        val paket= intent.getStringExtra("paket")

        val savePembayaran = hashMapOf(
            "user_uid" to user.uid,
            "paket" to paket,
            "harga" to harga,
        )

        //save ke langganan
        val LanggananDocument = db.collection("langganan").document(user.uid)
        LanggananDocument.set(savePembayaran).addOnSuccessListener {
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            val intent = Intent(this, TambahProduk::class.java)
            startActivity(intent)
            finish()
            sendNotification("Pembayaran Langganan", "Selamat Sekarang Anda Adalah Penjual Premium. Pembayaran Untuk Berlangganan ${paket} Berhasil")
            Toast.makeText(this, "Pembayaran Berhasil", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Log.w("TambahProduk", "failure : ", e)
        }

        val updateUser = hashMapOf(
            "as" to "penjual premium",
        )

        //update users
        val userDocument = db.collection("users").document(user.uid)
        userDocument.update(updateUser as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Selamat, Anda Sekarang Penjual Premium", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Log.w("update user", "failure : ", e)
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

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun pilihFoto() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 20 && resultCode == RESULT_OK && data != null) {
            val path: Uri? = data.data
            val thread = Thread {
                try {
                    val stream: InputStream? = contentResolver.openInputStream(path!!)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    binding.ivPembayaran.post {
                        nUser?.let { user ->
                            binding.ivPembayaran.setImageBitmap(bitmap)
                            uploadPembayaran()
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
                binding.ivPembayaran.post {
                    nUser?.let { user ->
                        binding.ivPembayaran.setImageBitmap(bitmap)
                        uploadPembayaran()
                    }
                }
            }
            thread.start()
        }

    }

    private fun uploadPembayaran() {
        binding.ivPembayaran.isDrawingCacheEnabled = true
        binding.ivPembayaran.buildDrawingCache(true)
        val bitmapDrawable = binding.ivPembayaran.drawable as BitmapDrawable
        val bitmap: Bitmap = bitmapDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)
        val second = currentTime.get(Calendar.SECOND)

        val reference: StorageReference = storage.child("Pembayaran_Belangganan/berlangganan_${nUser?.uid}_${hour}_${minute}_${second}.jpg")

        val uploadTask: UploadTask = reference.putBytes(data)
        uploadTask.addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
            progressDialog.dismiss()
            val downloadUrlTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    val dowloadUrl: Uri? = task.result
                    if (dowloadUrl != null) {
                        uri_pembayaran = dowloadUrl
                        url_pembayaran = uri_pembayaran.toString()
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

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, NotifikasiActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = this.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.tulisan_paskita)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSubText(getString(R.string.paskita))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            builder.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }

        val saveNotifikasi = Notifikasi(
            nama_notif = title,
            pesan = message,
            uid_user = nUser?.uid,
        )

        val transaksiDocument = db.collection("Notifikasi")
        transaksiDocument.add(saveNotifikasi)
            .addOnSuccessListener { documentReference ->
                Log.d("Notifikasi", "Berhasil Di Simpan")
            }
            .addOnFailureListener { e ->
                Log.w("Notifikasi", "failure : ", e)
            }
        val notification = builder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channel_01"
        private const val CHANNEL_NAME = "PasKita"
    }
}