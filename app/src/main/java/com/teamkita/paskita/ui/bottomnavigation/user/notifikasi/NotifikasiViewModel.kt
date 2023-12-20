package com.teamkita.paskita.ui.bottomnavigation.user.notifikasi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.teamkita.paskita.data.Notifikasi
import com.teamkita.paskita.data.Produk

class NotifikasiViewModel : ViewModel() {
    private val notifListLiveData = MutableLiveData<List<Notifikasi>>()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun fetchNotikasiList() {
        val produkCollection = db.collection("Notifikasi")
        val currentUser = auth.currentUser
        val query = produkCollection.whereEqualTo("uid_user", currentUser?.uid)

        query.get()
            .addOnSuccessListener { documents ->

                val notifList = mutableListOf<Notifikasi>()

                for (document in documents) {
                    val produk = document.toObject<Notifikasi>()
                    notifList.add(produk)
                }

                notifListLiveData.value = notifList
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    fun getNotifikasiListLiveData(): LiveData<List<Notifikasi>> {
        return notifListLiveData
    }
}