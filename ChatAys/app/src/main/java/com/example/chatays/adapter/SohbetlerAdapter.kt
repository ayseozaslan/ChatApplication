package com.example.chatays.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatays.R
import com.example.chatays.model.entities.ChatSummary

class SohbetlerAdapter (private val onItemClick: (ChatSummary) -> Unit)
    :RecyclerView.Adapter<SohbetlerAdapter.TalksCardHolder>(){

    private var talks: List<ChatSummary> = emptyList()

    inner class TalksCardHolder(itemView: View) :RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TalksCardHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.talks_card,parent,false)

        return TalksCardHolder(view)
    }

    override fun getItemCount(): Int = talks.size

    override fun onBindViewHolder(holder: TalksCardHolder, position: Int) {
        val talks = talks[position]
        holder.itemView.findViewById<TextView>(R.id.username_talks).text = talks.username
        holder.itemView.findViewById<TextView>(R.id.user_lastmessage_Talks).text = talks.lastMessage

        val imageView=holder.itemView.findViewById<ImageView>(R.id.imageViewUser_Talks)
        val imageUrl= talks.imageUrl

        if(!imageUrl.isNullOrEmpty()){
            Glide.with(holder.itemView.context.applicationContext)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.back_sign)
                .error(R.drawable.back_sign)
                .into(imageView)
        }else{
            imageView.setImageResource(R.drawable.back_sign)
        }

        holder.itemView.setOnClickListener {
            onItemClick(talks)
        }
    }

    fun  setData(list: List<ChatSummary>){
        talks= list
        notifyDataSetChanged()

    }
}