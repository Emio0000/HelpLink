package com.example.helplink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HelpRequestAdapter(
    private val list: MutableList<HelpRequest>
) : RecyclerView.Adapter<HelpRequestAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help_request, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description
        holder.tvEmail.text = "By: ${item.requesterEmail}"

        holder.btnAccept.setOnClickListener {
            val user = auth.currentUser ?: return@setOnClickListener

            db.collection("help_requests")
                .document(item.id)
                .update(
                    mapOf(
                        "status" to "accepted",
                        "helperId" to user.uid,
                        "helperEmail" to user.email
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(
                        holder.itemView.context,
                        "Request accepted",
                        Toast.LENGTH_SHORT
                    ).show()

                    list.removeAt(position)
                    notifyItemRemoved(position)
                }
                .addOnFailureListener {
                    Toast.makeText(
                        holder.itemView.context,
                        "Failed to accept",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
