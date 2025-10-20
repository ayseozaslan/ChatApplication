package com.example.chatays.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat.*
import com.example.chatays.R
import com.example.chatays.model.entities.User
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.DataSource


class ContactsAdapter (private val onItemClick: (User) -> Unit): RecyclerView.Adapter<ContactsAdapter.ContactsCardHolder>() {

    private var users: List<User> = emptyList()

    inner class ContactsCardHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsCardHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contacts_card, parent, false)
        return ContactsCardHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ContactsCardHolder, position: Int) {
        val user = users[position]

        holder.itemView.findViewById<TextView>(R.id.username_talks).text = user.username
        holder.itemView.findViewById<TextView>(R.id.user_lastmessage_Talks).text = user.email

        Log.d("ContactsAdapter", "Resim URL: ${user.username} -> ${user.imageUrl}")
       Log.d("Contacs", " Yükleniyor : ${user.username} -> ${user.imageUrl}")

        val imageView = holder.itemView.findViewById<ImageView>(R.id.imageViewUser_Talks)
        val imageUrl = user.imageUrl
        Log.d("GlideTest", "Yüklenen URL: $imageUrl")

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.back_sign)
                .error(R.drawable.back_sign)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        Log.e("GlideError", "Yüklenemedi: $imageUrl - ${e?.message}")
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        Log.d("GlideSuccess", "Yüklendi: $imageUrl")
                        return false                    }
                })
                .into(imageView)
        } else {
            Log.w("GlideSkip", "Geçersiz veya boş URL: ${user.username} - $imageUrl")
            Log.d("ImageCheck", "Kullanıcı: ${user.username}, URL: '$imageUrl'")
            imageView.setImageResource(R.drawable.back_sign)
        }

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    fun setData(list: List<User>) {
        users = list
        notifyDataSetChanged()
    }
}
