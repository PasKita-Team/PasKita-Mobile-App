package com.teamkita.paskita.ui.bottomnavigation.user.ratingdanulasan

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.data.Transaksi
import com.teamkita.paskita.data.UlasanProduk
import com.teamkita.paskita.databinding.ActivityUlasanBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.OnDialogCloseListener
import com.teamkita.paskita.ui.bottomnavigation.penjual.DashboardPenjual

class Ulasan : BottomSheetDialogFragment() {
    companion object {
        private const val ARG_TRANSAKSI = "arg_transaksi"

        fun newInstance(transaksi: Transaksi): Ulasan {
            val args = Bundle().apply {
                putParcelable(ARG_TRANSAKSI, transaksi)
            }
            val fragment = Ulasan()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Ulasan"
    }

    private lateinit var binding: ActivityUlasanBinding
    private val nUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityUlasanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAction()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            dismiss()
        }

        binding.btnKirim.setOnClickListener {
            val transaksi = arguments?.getParcelable<Transaksi>(ARG_TRANSAKSI)

            updateRating(transaksi)
        }

    }

    private fun updateRating(transaksi: Transaksi?) {

        val userDocument = transaksi?.id_produk?.let { db.collection("produk").document(it) }
        userDocument?.get()
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val terjual = userData?.get("terjual") as String
                    val updateTerjual = terjual.toInt() + 1

                    val updates = hashMapOf(
                        "terjual" to updateTerjual.toString(),
                        "ulasan_produk" to "liat ulasan"
                    )
                    userDocument
                        .update(updates as Map<String, Any>)
                        .addOnSuccessListener {

                            val ulasan = binding.etUlasan.text.toString()
                            val rating = binding.rating.rating

                            val saveUlasan = UlasanProduk(
                                id_produk = transaksi.id_produk,
                                pembeli = transaksi.namaPembeli,
                                ulasan = ulasan,
                                rating = rating,
                            )

                            db.collection("ulasan")
                                .add(saveUlasan)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Ulasan Berhasil Di Kirim", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(requireActivity(), BottomNavigation::class.java)
                                    startActivity(intent)
                                    dismiss()
                                }
                                .addOnFailureListener { e ->
                                    println("Error adding ulasan: $e")
                                }

                            val transaksiDocumentRef = db.collection("transaksi").document(transaksi.uid_pembeli+"_"+transaksi.namaProduk)

                            val update = hashMapOf(
                                "status" to "Selesai"
                            )

                            transaksiDocumentRef
                                .update(update as Map<String, Any>)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Transaksi Selesai, Terima Kasih Sudah Berbelanja Di PasKita:)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.addOnFailureListener { exception ->
                                    println("Error getting documents: $exception")
                                }
                        }.addOnFailureListener { exception ->
                            println("Error getting documents: $exception")
                        }

                }
            }
            ?.addOnFailureListener { e ->
                Log.w("GetUserData", "Gagal mengambil data: ", e)
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