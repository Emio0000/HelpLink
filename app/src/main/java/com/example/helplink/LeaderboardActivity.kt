package com.example.helplink

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LeaderboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val tvLeaderboard = findViewById<TextView>(R.id.tvLeaderboard)
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .orderBy("rewardPoints", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val builder = StringBuilder("🏆 Leaderboard\n\n")
                var rank = 1

                for (doc in result) {
                    val email = doc.getString("email") ?: "User"
                    val points = doc.getLong("rewardPoints") ?: 0
                    builder.append("$rank. $email — $points pts\n")
                    rank++
                }

                tvLeaderboard.text = builder.toString()
            }
    }
}
