package com.example.helplink

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ViewRequestsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private val requestList = mutableListOf<HelpRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_requests)

        val recyclerView = findViewById<RecyclerView>(R.id.rvRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = HelpRequestAdapter(requestList)
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()

        db.collection("help_requests")
            .whereEqualTo("status", "open")
            .get()
            .addOnSuccessListener { result ->
                requestList.clear()
                for (doc in result) {
                    val request = doc.toObject(HelpRequest::class.java)
                    request.id = doc.id
                    requestList.add(request)

                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show()
            }
    }
}
