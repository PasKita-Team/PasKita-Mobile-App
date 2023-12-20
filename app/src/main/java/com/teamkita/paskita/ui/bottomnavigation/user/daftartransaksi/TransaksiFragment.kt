package com.teamkita.paskita.ui.bottomnavigation.user.daftartransaksi

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.arlib.floatingsearchview.FloatingSearchView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.teamkita.paskita.R
import com.teamkita.paskita.data.Transaksi
import com.teamkita.paskita.databinding.FragmentTransaksiBinding
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.TransaksiAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.favorite.FavoriteProdukActivity
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangActivity
import com.teamkita.paskita.ui.bottomnavigation.user.notifikasi.NotifikasiActivity
import com.teamkita.paskita.ui.bottomnavigation.user.pesan.DaftarPesanActivity

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class TransaksiFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentTransaksiBinding? = null

    protected val RESULT_SPEECH = 1
    private val ID_BahasaIndonesia = "id"
    private val PERMISSION_REQUEST_CODE = 123

    private lateinit var listTransaksi: ArrayList<Transaksi>
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTransaksiBinding.bind(view)

        binding.rvTransaksi.layoutManager = LinearLayoutManager(requireActivity())
        listTransaksi = arrayListOf()
        setDataTransaksi()

        setupSearchBar()
        setupAction()


    }

    private fun setupAction() {

        binding.ivPesan.setOnClickListener {
            val intent = Intent(requireContext(), DaftarPesanActivity::class.java)
            startActivity(intent)

            activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }

        binding.ivNotif.setOnClickListener {
            val intent = Intent(requireContext(), NotifikasiActivity::class.java)
            startActivity(intent)

            activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }

        binding.ivKeranjang.setOnClickListener {
            val intent = Intent(requireContext(), KeranjangActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearchBar() {
        
        binding.searchBar.setOnQueryChangeListener(FloatingSearchView.OnQueryChangeListener { oldQuery, newQuery ->

            if (newQuery.isNullOrEmpty()) {
                setDataTransaksi()
            } else {
                searchTransaksi(newQuery)
            }

        })

        binding.searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.fav -> {
                    val intent = Intent(requireContext(), FavoriteProdukActivity::class.java)
                    startActivity(intent)
                }
                R.id.mic -> {
                    val permission = ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    )

                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
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
                                requireContext(),
                                "Maaf, Perangkat Anda Tidak Mendukung Speech To Text",
                                Toast.LENGTH_SHORT
                            ).show()
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun searchTransaksi(queryString: String) {
        val produkCollection = db.collection("transaksi")
        val currentUser = auth.currentUser
        val query = produkCollection.whereEqualTo("uid_pembeli", currentUser?.uid)
        query.get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    listTransaksi.clear()
                    for (data in it.documents){
                        val transaksi: Transaksi? = data.toObject(Transaksi::class.java)
                        if (transaksi?.namaProduk?.contains(queryString, ignoreCase = true) == true
                        ) {
                            listTransaksi.add(transaksi)
                        }
                    }
                    val transaksiAdapter = TransaksiAdapter()
                    transaksiAdapter.submitList(listTransaksi)
                    binding.rvTransaksi.adapter = transaksiAdapter
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    private fun setDataTransaksi() {
        val produkCollection = db.collection("transaksi")
        val currentUser = auth.currentUser
        val query = produkCollection.whereEqualTo("uid_pembeli", currentUser?.uid)
        query.get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    listTransaksi.clear()
                    for (data in it.documents){
                        val transaksi: Transaksi? = data.toObject(Transaksi::class.java)
                        if (transaksi != null) {
                            listTransaksi.add(transaksi)
                        }
                    }
                    val transaksiAdapter = TransaksiAdapter()
                    transaksiAdapter.submitList(listTransaksi)
                    binding.rvTransaksi.adapter = transaksiAdapter
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_SPEECH -> if (resultCode == Activity.RESULT_OK && data != null) {
                val text: ArrayList<String>? = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                text?.get(0)?.let { firstResult ->
                    binding.searchBar.setSearchText(firstResult)
                    searchTransaksi(firstResult)
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
                    requireContext(),
                    "Izin telah diberikan, Anda dapat menggunakan mikrofon",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "Izin mikrofon ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TransaksiFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}