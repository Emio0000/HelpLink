package com.example.helplink

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val btnRequestHelp = findViewById<Button>(R.id.btnRequestHelp)
        val btnOfferHelp = findViewById<Button>(R.id.btnOfferHelp)
        val btnMyJobs = findViewById<Button>(R.id.btnMyJobs)

        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = currentUser.uid

        // 🔥 LOAD USER INFO
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val email = document.getString("email")
                    val role = document.getString("role")

                    tvWelcome.text = "Welcome, $email"
                    tvRole.text = "Role: $role"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }

        // ✅ REQUEST HELP
        btnRequestHelp.setOnClickListener {
            startActivity(Intent(this, RequestHelpActivity::class.java))
        }

        // ✅ OFFER HELP → VIEW OPEN REQUESTS
        btnOfferHelp.setOnClickListener {
            startActivity(Intent(this, ViewRequestsActivity::class.java))
        }

        // ✅ MY JOBS → ACCEPTED REQUESTS
        btnMyJobs.setOnClickListener {
            startActivity(Intent(this, MyJobsActivity::class.java))
        }
    }
}
