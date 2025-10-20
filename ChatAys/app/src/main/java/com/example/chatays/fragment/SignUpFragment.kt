package com.example.chatays.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.chatays.R
import com.example.chatays.databinding.FragmentFeedBinding
import com.example.chatays.databinding.FragmentSignUpBinding
import com.example.chatays.utils.SignUpState
import com.example.chatays.viewModel.SignUpViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by viewModels()
    private var selectedProfileUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("SignUp", "pickImageLauncher uri: $uri")
        uri?.let {
            selectedProfileUri = it
            binding.imageUpdateSign.setImageURI(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.profileImageUrl.observe(viewLifecycleOwner) { selectedProfileUri ->
            Log.d("SignUp", "profileImageUrl observed: $selectedProfileUri")
            if (!selectedProfileUri.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(selectedProfileUri)
                    .circleCrop()
                    .into(binding.imageUpdateSign)
            } else {
                binding.imageUpdateSign.setImageResource(R.drawable.back_sign)
            }
        }

        viewModel.signUpState.observe(viewLifecycleOwner) { state ->
            Log.d("SignUp", "signUpState: $state")
            when (state) {
                is SignUpState.Idle -> {}
                is SignUpState.Loading -> { /* göstergeni göster */ }
                is SignUpState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    navigateToPerson()

                }
                is SignUpState.Error -> {
                    Toast.makeText(requireContext(), "Hata: ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.imageUpdateSign.setOnClickListener {
            Log.d("SignUp", "imageUpdateSign clicked")
            viewModel.onProfileImageClicked()
            pickImageLauncher.launch("image/*")
        }


        binding.signupButton.setOnClickListener {
            Log.d("SignUp", "signupButton clicked")
            val username = binding.usernameField.text.toString().trim()
            val email = binding.emailField.text.toString().trim()
            val password = binding.passwordField.editText?.text.toString().trim()

            Log.d("SignUp", "inputs -> username:$username email:$email passwordLen:${password.length}")

            if (!isPasswordValid(password)) {
                Toast.makeText(requireContext(), "Şifre en az 8 karakter olmalı ve ardışık rakam içermemeli", Toast.LENGTH_SHORT).show()
                Log.d("SignUp", "Şifre geçersiz")
            } else {
                viewModel.signUp(requireContext(), email, password, username, selectedProfileUri)
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false
        for (i in 0 until password.length - 2) {
            val first = password[i]
            val second = password[i + 1]
            val third = password[i + 2]
            if (first.isDigit() && second.isDigit() && third.isDigit()) {
                if (second - first == 1 && third - second == 1) return false
            }
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun navigateToPerson(){
        findNavController().navigate(R.id.signup_to_person)
    }
}
