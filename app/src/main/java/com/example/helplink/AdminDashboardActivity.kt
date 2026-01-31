package com.example.helplink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var rvPending: RecyclerView
    private lateinit var rvActive: RecyclerView

    private val db = FirebaseFirestore.getInstance()

    private val pendingList = mutableListOf<User>()
    private val activeList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        rvPending = findViewById(R.id.rvPending)
        rvActive = findViewById(R.id.rvActive)

        rvPending.layoutManager = LinearLayoutManager(this)
        rvActive.layoutManager = LinearLayoutManager(this)

        loadUsers()
    }

    private fun loadUsers() {

        db.collection("users")
            .addSnapshotListener { snapshot, _ ->

                pendingList.clear()
                activeList.clear()

                for (doc in snapshot!!) {

                    val user = User(
                        doc.id,
                        doc.getString("name") ?: "",
                        doc.getString("email") ?: "",
                        doc.getString("status") ?: ""
                    )

                    if (user.status == "pending") {
                        pendingList.add(user)
                    }

                    if (user.status == "active") {
                        activeList.add(user)
                    }
                }

                rvPending.adapter = UserAdapter(
                    pendingList,
                    true,
                    { approveUser(it) },
                    { deleteUser(it) }
                )

                rvActive.adapter = UserAdapter(
                    activeList,
                    false,
                    { },
                    { deleteUser(it) }
                )
            }
    }

    private fun approveUser(user: User) {
        db.collection("users")
            .document(user.uid)
            .update(
                mapOf(
                    "status" to "active",
                    "notify" to "approved"
                )
            )
    }

    private fun deleteUser(user: User) {
        db.collection("users")
            .document(user.uid)
            .delete()
    }
}
