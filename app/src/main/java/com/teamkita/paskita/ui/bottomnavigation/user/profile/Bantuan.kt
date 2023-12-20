package com.teamkita.paskita.ui.bottomnavigation.user.profile

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.databinding.ActivityBantuanBinding
import com.teamkita.paskita.ui.bottomnavigation.OnDialogCloseListener

class Bantuan : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "Bantuan"

        fun newInstance(): Bantuan {
            return Bantuan()
        }
    }

    private lateinit var binding: ActivityBantuanBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityBantuanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        binding.btnKirim.setOnClickListener {
            showLoading(true)
            val kendala = binding.etKendala.text.toString()
            val user = auth.currentUser
            user?.let {

                val kendalaUser = hashMapOf(
                    "email" to user.email,
                    "nama" to user.displayName,
                    "kendala" to kendala,
                )

                val db = FirebaseFirestore.getInstance()
                val userCollection = db.collection("kendala")

                userCollection.document(user.uid)
                    .set(kendalaUser)
                    .addOnSuccessListener {
                        showLoading(false)
                        dismiss()
                        Toast.makeText(requireContext(), "Kendala Anda Berhasil Di Kirim, Mohon Menunggu Team PasKita Membantu Anda Yaaaa. Silahkan Check Email Anda Untuk Solusinya", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        showLoading(false)
                        Log.w(TAG, "Gagal menyimpan kendala", e)
                    }
            }

        }

        setupAction()
        playAnimation()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            dismiss()
        }

    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val message =
            ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val kirim = ObjectAnimator.ofFloat(binding.btnKirim, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                message,
                emailTextView,
                emailEditTextLayout,
                kirim
            )
            startDelay = 100
        }.start()
    }

    private fun showLoading(state: Boolean) { binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity = requireActivity()
        if (activity is OnDialogCloseListener) {
            (activity as OnDialogCloseListener).onDialogClose(dialog)
        }
    }
}