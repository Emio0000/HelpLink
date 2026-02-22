package com.example.helplink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    private val list: List<Message>
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    override fun getItemViewType(position: Int): Int {
        val currentUid = auth.currentUser?.uid
        return if (list[position].senderId == currentUid) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layout = if (viewType == 1)
            R.layout.item_message_sent
        else
            R.layout.item_message_received

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvMessage.text = list[position].text
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
    }
}