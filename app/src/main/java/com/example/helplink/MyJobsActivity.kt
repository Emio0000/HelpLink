package com.example.helplink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyJobsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val jobList = mutableListOf<HelpRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_jobs)

        val recyclerView = findViewById<RecyclerView>(R.id.rvMyJobs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = MyJobsAdapter(jobList)
        recyclerView.adapter = adapter

        // Bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNav.selectedItemId = R.id.nav_jobs

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_chats -> {
                    startActivity(Intent(this, ChatsActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_maps -> {
                    startActivity(Intent(this, MapsActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_jobs -> true

                else -> false
            }
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser ?: return

        db.collection("help_requests")
            .whereEqualTo("helperId", user.uid)
            .whereEqualTo("status", "accepted")
            .get()
            .addOnSuccessListener { result ->

                jobList.clear()

                for (doc in result) {
                    val job = doc.toObject(HelpRequest::class.java)
                    job.id = doc.id
                    jobList.add(job)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load jobs", Toast.LENGTH_SHORT).show()
            }
    }
}