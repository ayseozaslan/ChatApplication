package com.example.chatays.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.chatays.R
import com.example.chatays.databinding.FragmentLoginBinding
import com.example.chatays.utils.LoginState
import com.example.chatays.viewModel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    private val requestPermissionLauncher=
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if(isGranted){
                Toast.makeText(requireContext(),"Rehber izni verildi",Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_login,container,false)

        checkContactsPermission()
        binding.loginfragment=this

        binding.loginButton.setOnClickListener {
            Log.d("LoginDebug", "Login butonuna tıklandı")

            val email = binding.emailField.text.toString().trim()
            val password = binding.passwordField.text.toString().trim()

            Log.d("LoginDebug", "Email: $email, Password: $password")

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, password )
            }
        }
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when(state) {
                is LoginState.Idle -> {
                }
                is LoginState.Loading -> {
                    Log.d("LoginFragment", "Current SignUpState: ${state.toString()}")
                }
                is LoginState.Success -> {
                    Log.d("LoginFragment", "Succeess SignUpState: ${state.toString()}")
                    navigationToPersons()
                }
                is LoginState.Error -> {
                    Toast.makeText(context, "Hata: ${state}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return binding.root
    }


    private fun checkContactsPermission(){
        if(ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED)
        {
             requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)

        }
    }


    fun navigationToPersons(){
        findNavController().navigate(R.id.login_to_person)

    }
    fun navigateToSignUp(){
        findNavController().navigate(R.id.login_to_signup)
    }
    fun navigateToTalks(){
        findNavController().navigate(R.id.login_to_talks)
    }


}