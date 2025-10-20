package com.example.chatays.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.chatays.R
import com.example.chatays.databinding.FragmentSettingsBinding
import com.example.chatays.viewModel.ProfilViewModel
import com.example.knowtopia.model.repository.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var db: FirebaseFirestore

    @Inject
    lateinit var authRepository: AuthRepository
    private val viewModel: ProfilViewModel by viewModels()

    private var selectedProfileUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("Settings", "pickImageLauncher uri: $uri")
        uri?.let {
            selectedProfileUri = it
            binding.imageUser.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.settings_to_talks)
        }

        binding.buttonSignout.setOnClickListener {
            authRepository.signOut()
            findNavController().navigate(R.id.setting_to_googlelogin)
        }
        loadUserData()
        binding.textuploadImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.buttonSettingSave.setOnClickListener {
            saveProfileImage()
        }
        viewModel.uploadImage.observe(viewLifecycleOwner) { imageUrl ->
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .circleCrop()
                    .into(binding.imageUser)
            } else {
                binding.imageUser.setImageResource(R.drawable.back_sign)
            }
        }
    }

    private fun loadUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Kullanıcı oturumu bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }

        val email = currentUser.email ?: ""
        val displayName = currentUser.displayName
        val imageUrl = currentUser.photoUrl?.toString()

        if (!displayName.isNullOrEmpty()) {
            binding.editTextEmail.setText(email)
            binding.editTextUsername.setText(displayName)
            loadProfileImage(imageUrl)
            Log.d("Settings", "Google kullanıcısı: $displayName")
        } else {
            val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val userRef= FirebaseFirestore.getInstance().collection("Users").document(currentUser)

            userRef.get().addOnSuccessListener { document ->
                val email= document.getString("email") ?: ""
                val username =  document.getString("username") ?: ""
                val image = document.getString("imageUrl")   ?: ""
                binding.apply {
                    editTextEmail.setText(email)
                    editTextUsername.setText(username)
                }
                Glide.with(binding.imageUser)
                    .load(image)
                    .placeholder(R.drawable.back_sign)
                    .error(R.drawable.back_sign)
                    .circleCrop()
                    .into(binding.imageUser)

            }
        }
    }

    private fun loadProfileImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.back_sign)
                .error(R.drawable.back_sign)
                .circleCrop()
                .into(binding.imageUser)
        } else {
            binding.imageUser.setImageResource(R.drawable.back_sign)
        }
    }

    /**
     * 🔹 Seçilen profil fotoğrafını yükler ve ViewModel'e yollar
     */
    private fun saveProfileImage() {
        val selectedUri = selectedProfileUri
        val context = requireContext()

        if (selectedUri != null) {
            val imageBytes = context.contentResolver.openInputStream(selectedUri)?.use { it.readBytes() }

            if (imageBytes != null) {
                val timestamp = (System.currentTimeMillis() / 1000).toString()
                val signature = "SIGNATURE_FROM_YOUR_SERVER" // Gerçek signature backend’den alınmalı

                viewModel.uploadProfileImage(
                    imageBytes = imageBytes,
                    signature = signature,
                    timestamp = timestamp,
                    onSuccess = {
                        Toast.makeText(context, "Profil fotoğrafı güncellendi", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Profil fotoğrafı güncellenemedi: $error", Toast.LENGTH_SHORT).show()
                        Log.e("ProfilHata", error)
                    }
                )
            } else {
                Toast.makeText(context, "Görsel okunamadı", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Görsel seçilmedi", Toast.LENGTH_SHORT).show()
        }
    }
}
