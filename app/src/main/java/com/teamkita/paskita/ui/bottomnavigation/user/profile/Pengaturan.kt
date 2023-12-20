package com.teamkita.paskita.ui.bottomnavigation.user.profile

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.teamkita.paskita.databinding.ActivityPengaturanBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.OnDialogCloseListener
import com.teamkita.paskita.ui.logindanregister.lupapassword.UbahPassword

class Pengaturan : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "Pengaturan"

        fun newInstance(): Pengaturan {
            return Pengaturan()
        }
    }

    private lateinit var binding: ActivityPengaturanBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityPengaturanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvUbahPassword.setOnClickListener {
            startActivity(Intent(requireActivity(), UbahPassword::class.java))
            requireActivity().finish()
        }

        auth = Firebase.auth

        binding.tvKeluar.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireActivity(), BottomNavigation::class.java))
            requireActivity().finish()
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