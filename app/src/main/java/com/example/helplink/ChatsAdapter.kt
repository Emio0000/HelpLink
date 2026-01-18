package com.example.helplink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatsAdapter(
    private val list: List<ChatRoom>,
    private val onClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvChatTitle)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = list[position]
        val currentUserId = auth.currentUser?.uid ?: ""

        // ✅ SHOW EMAIL INSTEAD OF JOB ID
        holder.tvTitle.text = "Chat with ${chat.getOtherUserEmail(currentUserId)}"

        holder.tvLastMessage.text =
            if (chat.lastMessage.isEmpty()) "No messages yet"
            else chat.lastMessage

        holder.itemView.setOnClickListener {
            onClick(chat)
        }
    }
}
