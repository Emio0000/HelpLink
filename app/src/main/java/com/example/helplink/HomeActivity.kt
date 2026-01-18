package com.example.helplink

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

        // UI
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val tvRewardPoints = findViewById<TextView>(R.id.tvRewardPoints)
        val tvBadge = findViewById<TextView>(R.id.tvBadge)

        val btnRequestHelp = findViewById<Button>(R.id.btnRequestHelp)
        val btnOfferHelp = findViewById<Button>(R.id.btnOfferHelp)
        val btnMyJobs = findViewById<Button>(R.id.btnMyJobs)
        val btnLeaderboard = findViewById<Button>(R.id.btnLeaderboard)
        val btnChats = findViewById<Button>(R.id.btnChats)
        val btnMaps = findViewById<Button>(R.id.btnMaps)

        val user = auth.currentUser ?: return
        val uid = user.uid

        // 🔥 Load user data
        db.collection("users")
            .document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {

                    val email = doc.getString("email") ?: "User"
                    val role = doc.getString("role") ?: "user"
                    val points = doc.getLong("rewardPoints") ?: 0

                    tvWelcome.text = "Welcome, $email"
                    tvRole.text = "Role: $role"
                    tvRewardPoints.text = points.toString()
                    tvBadge.text = getBadge(points)
                }
            }

        // 🔘 Navigation buttons
        btnRequestHelp.setOnClickListener {
            startActivity(Intent(this, RequestHelpActivity::class.java))
        }

        btnOfferHelp.setOnClickListener {
            startActivity(Intent(this, ViewRequestsActivity::class.java))
        }

        btnMyJobs.setOnClickListener {
            startActivity(Intent(this, MyJobsActivity::class.java))
        }

        btnLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        btnChats.setOnClickListener {
            startActivity(Intent(this, ChatsActivity::class.java))
        }

        btnMaps.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    private fun getBadge(points: Long): String {
        return when {
            points >= 100 -> "Badge: Gold 🥇"
            points >= 50 -> "Badge: Silver 🥈"
            else -> "Badge: Bronze 🥉"
        }
    }
}
