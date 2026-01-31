package com.example.helplink

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MyJobsAdapter(
    private val list: MutableList<HelpRequest>
) : RecyclerView.Adapter<MyJobsAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val btnChat: Button = itemView.findViewById(R.id.btnChat)
        val btnComplete: Button = itemView.findViewById(R.id.btnComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_job, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val job = list[position]

        holder.tvTitle.text = job.title
        holder.tvDescription.text = job.description
        holder.tvEmail.text = "Requester: ${job.requesterEmail}"

        // ---------------- CHAT ----------------
        holder.btnChat.setOnClickListener {

            val context = holder.itemView.context

            db.collection("chats")
                .document(job.id)
                .set(
                    mapOf(
                        "taskId" to job.id,
                        "requesterEmail" to job.requesterEmail,
                        "helperId" to job.helperId
                    ),
                    SetOptions.merge()
                )

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("taskId", job.id)
            context.startActivity(intent)
        }

        // ------------ COMPLETE JOB -------------
        holder.btnComplete.setOnClickListener {

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val context = holder.itemView.context

            val jobRef = db.collection("help_requests").document(job.id)
            val userRef = db.collection("users").document(uid)

            db.runTransaction { transaction ->

                val jobSnap = transaction.get(jobRef)
                val userSnap = transaction.get(userRef)

                if (jobSnap.getBoolean("rewardGiven") == true) {
                    return@runTransaction null
                }

                val points = userSnap.getLong("rewardPoints") ?: 0
                val completed = userSnap.getLong("completedTasks") ?: 0

                transaction.update(
                    jobRef,
                    mapOf(
                        "status" to "completed",
                        "rewardGiven" to true
                    )
                )

                transaction.update(
                    userRef,
                    mapOf(
                        "rewardPoints" to points + 10,
                        "completedTasks" to completed + 1
                    )
                )

                null

            }.addOnSuccessListener {

                Toast.makeText(
                    context,
                    "Job completed +10 points 🎉",
                    Toast.LENGTH_SHORT
                ).show()

                val index = holder.adapterPosition

                if (index != RecyclerView.NO_POSITION) {
                    list.removeAt(index)
                    notifyItemRemoved(index)
                }

                // Go Home ONLY if list empty
                if (list.isEmpty()) {
                    context.startActivity(
                        Intent(context, HomeActivity::class.java)
                    )
                }
            }
        }
    }
}
