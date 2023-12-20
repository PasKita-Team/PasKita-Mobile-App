package com.teamkita.paskita.ui.bottomnavigation.user.keranjang

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.teamkita.paskita.data.Produk

class KeranjangViewModel : ViewModel() {
    private val produkListLiveData = MutableLiveData<List<Produk>>()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    var number: Int = 1

    val currentNumber: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun searchProduk(query: String?): LiveData<List<Produk>> {
        val matchingProdukLiveData = MutableLiveData<List<Produk>>()
        val currentUser = auth.currentUser
        val produkCollection = db.collection("produk").whereEqualTo("keranjang_by", currentUser?.uid)

        query?.let { queryString ->
            produkCollection.get()
                .addOnSuccessListener { documents ->
                    val matchingProdukList = mutableListOf<Produk>()
                    for (document in documents) {
                        val produkData = document.toObject<Produk>()
                        if (produkData.nama_produk?.contains(queryString, ignoreCase = true) == true ||
                            produkData.kategori_produk == queryString
                        ) {
                            matchingProdukList.add(produkData)
                        }
                    }
                    matchingProdukLiveData.value = matchingProdukList
                }
                .addOnFailureListener { exception ->
                    println("Error getting documents: $exception")
                }
        } ?: run {
            // Handle null query here if needed
        }
        return matchingProdukLiveData
    }

    fun fetchProdukList() {
        val produkCollection = db.collection("produk")
        val currentUser = auth.currentUser
        val query = produkCollection.whereEqualTo("keranjang_by", currentUser?.uid)

        query.get()
            .addOnSuccessListener { documents ->
                val produkList = mutableListOf<Produk>()
                for (document in documents) {
                    val produkData = document.toObject<Produk>()
                    produkList.add(produkData)
                }
                produkListLiveData.value = produkList // Mengatur data produk ke LiveData
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    fun getProdukListLiveData(): LiveData<List<Produk>> {
        return produkListLiveData
    }
}