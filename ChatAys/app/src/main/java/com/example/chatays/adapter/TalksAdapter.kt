package com.example.chatays.adapter

import android.provider.ContactsContract.Data
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatays.R
import com.example.chatays.fragment.ImageDialogFragment
import com.example.chatays.model.entities.Chat
import com.example.chatays.model.entities.ChatMessage
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth


class TalksAdapter(private val currentUser: String,
                   private val fragment: Fragment,
) :
    RecyclerView.Adapter<TalksAdapter.ChatHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2


    private val diffUtil = object : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this, diffUtil)
    var chats: List<ChatMessage>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    inner class ChatHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemViewType(position: Int): Int {
        val chat = chats[position]
        return if (chat.user1Id == currentUser) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val layout = if (viewType == VIEW_TYPE_SENT) R.layout.chat_right else R.layout.chat_row
        return ChatHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val chat = chats[position]
        val textView = holder.itemView.findViewById<TextView>(R.id.chatRvTextView)
        // val senderName = if (chat.user1Id == currentUser) currentUser else chat.user1Id
        textView.text = " ${chat.text}"
        val  chatImage= holder.itemView.findViewById<ImageView>(R.id.chatImage)
        if(!chat.imageUrl.isNullOrEmpty()){
            chatImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(chat.imageUrl)
                .centerCrop()
                .into(chatImage)
        }else{
            chatImage.visibility = View.GONE
        }

        chatImage.setOnClickListener {
            val dialog = ImageDialogFragment.newInstance(chat.imageUrl!!)
            dialog.show(fragment.parentFragmentManager, "ImageDialog")
        }


    }

    override fun getItemCount() = chats.size
}
