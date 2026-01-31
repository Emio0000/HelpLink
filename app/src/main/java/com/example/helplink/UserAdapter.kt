package com.example.helplink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val list: List<User>,
    private val showApprove: Boolean,
    private val onApprove: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvName)
        val email: TextView = v.findViewById(R.id.tvEmail)
        val approve: Button = v.findViewById(R.id.btnApprove)
        val delete: Button = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, i: Int) {
        val user = list[i]

        h.name.text = user.name
        h.email.text = user.email

        h.approve.visibility = if (showApprove) View.VISIBLE else View.GONE

        h.approve.setOnClickListener {
            onApprove(user)
        }

        h.delete.setOnClickListener {
            onDelete(user)
        }
    }

    override fun getItemCount() = list.size
}
