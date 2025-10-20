package com.example.chatays.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.chatays.R
import com.example.chatays.model.entities.Chat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter: RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

    private val VIEW_TYPE_MESSAGE_SENT = 1 //mesaj gönderen
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2 //mesaj alan

    inner class ChatHolder(itemView:View): RecyclerView.ViewHolder(itemView){

    }


    private val diffUtil = object : DiffUtil.ItemCallback<Chat>(){
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {

            return oldItem == newItem
        }
    }

    private val recycleListDiffer = AsyncListDiffer(this,diffUtil)
    var chats: List<Chat>
        get() = recycleListDiffer.currentList
        set(value) = recycleListDiffer.submitList(value)


    override fun getItemViewType(position: Int): Int {

        val chat = chats.get(position)
        if(chat.username == FirebaseAuth.getInstance().currentUser?.displayName){
            return VIEW_TYPE_MESSAGE_SENT
        }else{
            return VIEW_TYPE_MESSAGE_RECEIVED
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {

        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_row,parent,false)
            return ChatHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_right,parent,false)
            return ChatHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val text= holder.itemView.findViewById<TextView>(R.id.chatRvTextView)
        text.text = " ${ chats.get(position).username}: ${chats.get(position).text}" //kullanıcının adı ve mesajı

    }
}