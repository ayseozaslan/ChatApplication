package com.example.chatays.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatays.R
import com.example.chatays.adapter.TalksAdapter
import com.example.chatays.databinding.FragmentChatBinding
import com.example.chatays.viewModel.ChatViewModel
import com.example.chatays.viewModel.SignUpViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider


@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModelChat: ChatViewModel by viewModels()
    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var adapter: TalksAdapter
    private lateinit var auth: FirebaseAuth

    private lateinit var user2Username: String
    private lateinit var chatRoomId: String

    private var selectedImageUri: Uri? = null

    private lateinit var emojiPopup: EmojiPopup


    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreview.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.chatfragment = this
        auth = Firebase.auth

        val args: ChatFragmentArgs by navArgs()
        user2Username = args.username
        val imageUrl = args.imageUrl

        binding.chatTextUsername.text = user2Username
        Glide.with(requireContext())
            .load(imageUrl)
            .circleCrop()
            .placeholder(R.drawable.back_sign)
            .into(binding.chatImage)

        binding.imagePreview.visibility = View.GONE

        EmojiManager.install(GoogleEmojiProvider())


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = auth.currentUser?.displayName ?: return
        chatRoomId = getChatRoomId(currentUser, user2Username)

        adapter = TalksAdapter(currentUser, this)
        binding.chatRvPerson.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = this@ChatFragment.adapter
        }

        // Mesajları dinle
        viewModel.listenToChatRoom(chatRoomId)
        viewModel.messages.observe(viewLifecycleOwner) { chats ->
            adapter.chats = chats
            binding.chatRvPerson.scrollToPosition(chats.size - 1)
        }


        //Root view'i alıyoruz
        val rootView = requireActivity().findViewById<View>(android.R.id.content)

        // EmojiPopup'u oluşturuyoruz
        emojiPopup = EmojiPopup.Builder
            .fromRootView(rootView)
            .setOnEmojiClickListener { _, emoji ->
                val cursorPos = binding.chatTextPerson.selectionStart
                binding.chatTextPerson.text?.insert(cursorPos, emoji.unicode)
            }
            .build(binding.chatTextPerson)

        // Sadece buttonEmoji tıklanırsa aç/kapat
        binding.buttonEmoji.setOnClickListener {
            emojiPopup.toggle() // klavye ile ilgilenmeye gerek yok
        }
        binding.sendButtonPerson.setOnClickListener {
            val text = binding.chatTextPerson.text.toString()

            if (selectedImageUri != null) {
                viewModelChat.uploadImageToCloudinary(
                    context = requireContext(),
                    imageUri = selectedImageUri!!
                ) { imageUrl ->
                    viewModel.sendMessage(
                        text = text,
                        imageUrl = imageUrl,
                        chatRoomId = chatRoomId,
                        sender = currentUser,
                        receiver = user2Username,
                    )
                }

                binding.imagePreview.setImageURI(null)
                binding.imagePreview.visibility = View.GONE
                selectedImageUri = null
            } else if (text.isNotEmpty()) {
                // Sadece text mesajı
                viewModel.sendMessage(
                    text = text,
                    imageUrl = null,
                    chatRoomId= chatRoomId,
                    sender=currentUser,
                    receiver = user2Username
                )
            }

            binding.chatTextPerson.text?.clear()
        }

        binding.imageCamera.setOnClickListener {
            pickImageLauncher.launch("image/*")

        }

        viewModelChat.listenToTyping(chatRoomId, user2Username)
        viewModelChat.otherUserTyping.observe(viewLifecycleOwner) { isTyping ->
            binding.textTypingStatus.visibility = if (isTyping) View.VISIBLE else View.GONE
        }

        binding.chatTextPerson.addTextChangedListener(object : TextWatcher {
            private var typing = false
            private val handler = Handler(Looper.getMainLooper())
            private val runnable = Runnable {
                typing = false
                viewModelChat.setTypingState(chatRoomId, currentUser, false)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!typing) {
                    typing = true
                    viewModelChat.setTypingState(chatRoomId, currentUser, true)
                    handler.removeCallbacks(runnable)

                    handler.postDelayed(runnable, 1500)
                } else {
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 1500)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun getChatRoomId(user1: String, user2: String): String {
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun navigateToTalks() {
        findNavController().navigate(R.id.chat_to_talks)
    }
}
