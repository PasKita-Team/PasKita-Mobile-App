package com.teamkita.paskita.ui.bottomnavigation.user.ratingdanulasan

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.data.UlasanProduk
import com.teamkita.paskita.databinding.ActivityRatingBinding
import com.teamkita.paskita.ui.bottomnavigation.OnDialogCloseListener
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.RatingAdapter

class Rating : BottomSheetDialogFragment() {
    companion object {
        private const val ARG_RATING = "arg_Rating"

        fun newInstance(produk: Produk): Rating {
            val args = Bundle().apply {
                putParcelable(ARG_RATING, produk)
            }
            val fragment = Rating()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Rating"
    }

    private lateinit var binding: ActivityRatingBinding
    private lateinit var listRating: ArrayList<UlasanProduk>
    private var db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){
            rvRating.layoutManager = LinearLayoutManager(requireActivity())
            listRating = arrayListOf()
            db = FirebaseFirestore.getInstance()

            val produk = arguments?.getParcelable<Produk>(ARG_RATING)
            if (produk != null) {
                setRatingData(produk)
            }
        }

        setupAction()
    }

    private fun setRatingData(produk: Produk) {
        val favCollection = db.collection("ulasan")
        val query = favCollection.whereEqualTo("id_produk", produk.id_produk)
        query.get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    listRating.clear()
                    for (data in it.documents){
                        val ulasan: UlasanProduk? = data.toObject(UlasanProduk::class.java)
                        if (ulasan != null) {
                            listRating.add(ulasan)
                        }
                    }
                    binding.rvRating.adapter = RatingAdapter(listRating)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),it.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            dismiss()
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