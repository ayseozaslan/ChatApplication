package com.example.chatays.fragment

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatays.R
import com.example.chatays.adapter.ChatAdapter
import com.example.chatays.databinding.FragmentFeedBinding
import com.example.chatays.model.entities.Chat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.widget.NestedScrollView
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.chatays.viewModel.SignUpViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private  val viewModel : SignUpViewModel by viewModels()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter : ChatAdapter
    private var chats = arrayListOf<Chat>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding=DataBindingUtil.inflate(inflater,R.layout.fragment_feed,container,false)
        firestore= Firebase.firestore
        db= Firebase.firestore
        auth= Firebase.auth



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.profileImageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {

                Glide.with(requireContext())
                    .load(url)
                    .circleCrop()
                    .into(binding.imageUser)
            } else {

                binding.imageUser.setImageResource(R.drawable.back_sign)
            }
        }

        val userId = auth.currentUser?.uid ?: return

            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val imageUrl = document.getString("imageUrl")
                        val username = document.getString("username")


                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(imageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.back_sign)
                                .error(R.drawable.back_sign)
                                .into(binding.imageUser)
                        }


                        binding.usernameText.text = username
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FeedFragment", "❌ Firestore hata: ${e.message}")
                }


        adapter = ChatAdapter()
        binding.chatRv.setHasFixedSize(true)
        binding.chatRv.adapter = adapter
        binding.chatRv.layoutManager = LinearLayoutManager(requireContext())

        binding.chatText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboardAndScroll()
            }
        }
        
        binding.chatText.setOnClickListener {
            showKeyboardAndScroll()
        }
        binding.root.setOnClickListener {
            hideKeyboard()
        }
        binding.usernameText.text= FirebaseAuth.getInstance().currentUser?.displayName.toString()


        binding.sendButton.setOnClickListener {
            val chatText=binding.chatText.text.toString()
            val username = FirebaseAuth.getInstance().currentUser?.displayName.toString()
            val date = FieldValue.serverTimestamp()

            val dataMap=HashMap<String, Any>()
            dataMap.put("text",chatText)
            dataMap.put("username",username)
            dataMap.put("date", date)

            firestore.collection("Chats").add(dataMap).addOnSuccessListener {
                 binding.chatText.setText("")
            }
                .addOnFailureListener {
                    Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
                    binding.chatText.setText("")
                }
        }

        firestore.collection("Chats").orderBy("date",Query.Direction.ASCENDING).addSnapshotListener { value, error ->
             if(error != null){
                 Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_SHORT)
                     .show()
             }else{
                 if(value != null){
                     if(value.isEmpty){
                         Toast.makeText(requireContext(),"Mesaj Yok",Toast.LENGTH_SHORT).show()
                     }else{
                         val documents = value.documents
                         chats.clear()
                         for(document in documents){//tek tek documnetleri al.
                             val text  =document.get("text") as String
                             val username= document.get("username") as String
                             //println(text)

                             val chat= Chat(document.id,username)
                             chats.add(chat)
                             adapter.chats=chats
                             
                             // Debugging logları ekle
                             val currentUserDisplayName = FirebaseAuth.getInstance().currentUser?.displayName
                             Log.d("ChatDebug", "Current User Display Name: $currentUserDisplayName")
                             Log.d("ChatDebug", "Chat Username: ${chat.username}")
                             Log.d("ChatDebug", "Is Sender: ${chat.username == currentUserDisplayName}")

                         }
                     }
                     adapter.notifyDataSetChanged()
                 }
             }
        }
    }

    private fun showKeyboardAndScroll() {
        binding.chatText.requestFocus()
        
        // Klavyeyi göster
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.chatText, InputMethodManager.SHOW_IMPLICIT)

    }
    
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.chatText.clearFocus()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}