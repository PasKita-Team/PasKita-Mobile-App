package com.teamkita.paskita.ui.bottomnavigation.user.keranjang

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.teamkita.paskita.data.Keranjang
import com.teamkita.paskita.data.Produk

class KeranjangViewModel : ViewModel() {
    private val produkListLiveData = MutableLiveData<List<Produk>>()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    val produkList = mutableListOf<Produk>()
    val matchingProdukList = mutableListOf<Produk>()

    var number: Int = 1

    val currentNumber: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun searchProduk(query: String?): LiveData<List<Produk>> {
        val matchingProdukLiveData = MutableLiveData<List<Produk>>()
        val currentUser = auth.currentUser
        val prdkCollection = db.collection("keranjang")
            .whereEqualTo("keranjang_by", currentUser?.uid)
        query?.let { queryString ->
            prdkCollection.get()
                .addOnSuccessListener { documents ->
                    for (data in documents){
                        matchingProdukList.clear()
                        val keranjang: Keranjang = data.toObject(Keranjang::class.java)
                        val id_produk = keranjang.id_produk
                        val produkCollection = db.collection("produk").document(id_produk.toString())
                        produkCollection.get()
                            .addOnSuccessListener { produkDocument ->
                                val produkData = produkDocument.toObject<Produk>()
                                if (produkData?.nama_produk?.contains(queryString, ignoreCase = true) == true
                                ) {
                                    matchingProdukList.add(produkData)
                                }
                                matchingProdukLiveData.value = matchingProdukList
                            }
                            .addOnFailureListener { exception ->
                                println("Error getting documents: $exception")
                            }
                    }

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
        val currentUser = auth.currentUser
        val prdkCollection = db.collection("keranjang")
            .whereEqualTo("keranjang_by", currentUser?.uid)
        prdkCollection.get()
            .addOnSuccessListener { documents ->
                for (data in documents){
                    produkList.clear()
                    val keranjang: Keranjang = data.toObject(Keranjang::class.java)
                    val id_produk = keranjang.id_produk
                    val produkCollection = db.collection("produk").document(id_produk.toString())
                    produkCollection.get()
                        .addOnSuccessListener { produkDocument ->
                            val produkData = produkDocument.toObject<Produk>()
                            if (produkData != null){
                                produkList.add(produkData)
                            }
                            produkListLiveData.value = produkList
                        }
                        .addOnFailureListener { exception ->
                            println("Error getting documents: $exception")
                        }

                }

            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    fun getProdukListLiveData(): LiveData<List<Produk>> {
        return produkListLiveData
    }
}