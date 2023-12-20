package com.teamkita.paskita.ui.bottomnavigation.penjual

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.teamkita.paskita.data.HasilGenerate
import com.teamkita.paskita.data.Produk

class TambahProdukViewModel : ViewModel() {
    private val generateListLiveData = MutableLiveData<List<HasilGenerate>>()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    val produkList = mutableListOf<HasilGenerate>()

    fun fetchTemplateList() {
        produkList.clear()
        val currentUser = auth.currentUser
        val prdkCollection = db.collection("hasil_generated")
            .whereEqualTo("user_id", currentUser?.uid)

        // Menggunakan addSnapshotListener untuk mendengarkan perubahan data pada koleksi
        prdkCollection.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                println("Error listening for changes: $exception")
                return@addSnapshotListener
            }

            // Bersihkan produkList sebelum menambahkan data baru
            produkList.clear()

            // Jika tidak ada kesalahan dan ada data yang berubah
            if (snapshots != null && !snapshots.isEmpty) {
                for (data in snapshots) {
                    val hasilGenerate: HasilGenerate = data.toObject<HasilGenerate>()
                    produkList.add(hasilGenerate)
                }
                // Setelah mengupdate produkList, perbarui LiveData
                generateListLiveData.value = produkList
            } else {
                // Jika tidak ada data atau data kosong, atur LiveData menjadi null atau empty list
                generateListLiveData.value = emptyList()
            }
        }
    }

    fun getGenerateListLiveData(): LiveData<List<HasilGenerate>> {
        return generateListLiveData
    }

    val url_produk: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

}