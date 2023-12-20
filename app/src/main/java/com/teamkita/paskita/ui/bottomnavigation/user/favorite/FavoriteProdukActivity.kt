package com.teamkita.paskita.ui.bottomnavigation.user.favorite

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.arlib.floatingsearchview.FloatingSearchView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.teamkita.paskita.R
import com.teamkita.paskita.data.Favorite
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ActivityFavoriteProdukBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.ProdukAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangActivity

class FavoriteProdukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteProdukBinding

    protected val RESULT_SPEECH = 1
    private val ID_BahasaIndonesia = "id"
    private val PERMISSION_REQUEST_CODE = 123

    private lateinit var listProduk: ArrayList<Produk>
    private var db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            rvFavorite.layoutManager = GridLayoutManager(this@FavoriteProdukActivity,2)
            listProduk = arrayListOf()
            db = FirebaseFirestore.getInstance()

            setFavoriteData()
        }

        setupAction()
    }

    private fun setupAction() {

        binding.searchBar.setOnQueryChangeListener(FloatingSearchView.OnQueryChangeListener { oldQuery, newQuery ->

            if (newQuery.isNullOrEmpty()) {
                setFavoriteData()
            } else {
                searchFavorite(newQuery)
            }

        })

        binding.searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.keranjang -> {
                    val intent = Intent(applicationContext, KeranjangActivity::class.java)
                    startActivity(intent)
                }
                R.id.mic -> {
                    val permission = ContextCompat.checkSelfPermission(
                        applicationContext!!,
                        Manifest.permission.RECORD_AUDIO
                    )

                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            PERMISSION_REQUEST_CODE
                        )
                    } else {
                        val mic_google = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        mic_google.putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        mic_google.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ID_BahasaIndonesia)
                        try {
                            startActivityForResult(
                                mic_google,
                                RESULT_SPEECH
                            )
                            binding.searchBar.setSearchText("")
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                applicationContext,
                                "Maaf, Perangkat Anda Tidak Mendukung Speech To Text",
                                Toast.LENGTH_SHORT
                            ).show()
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        binding.ivBack.setOnClickListener {
            val intent = Intent(this, BottomNavigation::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setFavoriteData() {
        db = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        val favCollection = db.collection("favorite")
        val query = favCollection.whereEqualTo("favorite_by", user?.uid)
        query.get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    for (data in it.documents){
                        listProduk.clear()
                        val favorite: Favorite? = data.toObject(Favorite::class.java)
                        if (favorite != null){
                            val id_produk = favorite.id_produk
                            val produkCollection = db.collection("produk").document(id_produk.toString())
                            produkCollection.get()
                                .addOnSuccessListener { produkDocument ->
                                    if (produkDocument.exists()) {
                                        val produk: Produk? = produkDocument.toObject(Produk::class.java)
                                        if (produk != null) {
                                            listProduk.add(produk)
                                        }
                                        binding.rvFavorite.adapter = ProdukAdapter(listProduk)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(applicationContext, exception.toString(), Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    binding.rvFavorite.adapter = ProdukAdapter(listProduk)
                }
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,it.toString(), Toast.LENGTH_SHORT).show()
            }
    }
    private fun searchFavorite(queryString: String) {
        db = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        val favCollection = db.collection("favorite")
        val query = favCollection.whereEqualTo("favorite_by", user?.uid)
        query.get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    for (data in it.documents){
                        val favorite: Favorite? = data.toObject(Favorite::class.java)
                        if (favorite != null){
                            val id_produk = favorite.id_produk
                            val produkCollection = db.collection("produk").document(id_produk.toString())
                            produkCollection.get()
                                .addOnSuccessListener { produkDocument ->
                                    if (produkDocument.exists()) {
                                        listProduk.clear()
                                        val produk: Produk? = produkDocument.toObject(Produk::class.java)
                                        if (produk != null) {
                                            listProduk.add(produk)
                                        }
                                        binding.rvFavorite.adapter = ProdukAdapter(listProduk)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(applicationContext, exception.toString(), Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    binding.rvFavorite.adapter = ProdukAdapter(listProduk)
                }
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext,it.toString(), Toast.LENGTH_SHORT).show()
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_SPEECH -> if (resultCode == Activity.RESULT_OK && data != null) {
                val text: ArrayList<String>? = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                text?.get(0)?.let { firstResult ->
                    binding.searchBar.setSearchText(firstResult)
                    searchFavorite(firstResult)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    applicationContext,
                    "Izin telah diberikan, Anda dapat menggunakan mikrofon",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(applicationContext, "Izin mikrofon ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

}