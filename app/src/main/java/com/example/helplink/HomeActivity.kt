package com.example.helplink

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        var loginNotified = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val uid = user.uid

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                db.collection("users")
                    .document(uid)
                    .update("fcmToken", token)
            }

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val tvRewardPoints = findViewById<TextView>(R.id.tvRewardPoints)
        val tvBadge = findViewById<TextView>(R.id.tvBadge)
        val tvAvatar = findViewById<TextView>(R.id.tvAvatar)

        val btnOfferHelp = findViewById<Button>(R.id.btnOfferHelp)
        val btnMyJobs = findViewById<Button>(R.id.btnMyJobs)
        val btnLeaderboard = findViewById<Button>(R.id.btnLeaderboard)
        val btnChats = findViewById<Button>(R.id.btnChats)
        val btnMaps = findViewById<Button>(R.id.btnMaps)

        val fab = findViewById<FloatingActionButton>(R.id.fabRequestHelp)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> true

                R.id.nav_chats -> {
                    startActivity(Intent(this, ChatsActivity::class.java))
                    true
                }

                R.id.nav_maps -> {
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
                }

                R.id.nav_jobs -> {
                    startActivity(Intent(this, MyJobsActivity::class.java))
                    true
                }

                else -> false
            }
        }

        fab.setOnClickListener {
            startActivity(Intent(this, RequestHelpActivity::class.java))
        }

        db.collection("users")
            .document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {

                    val email = doc.getString("email") ?: "User"
                    val role = doc.getString("role") ?: "user"
                    val points = doc.getLong("rewardPoints") ?: 0
                    val notify = doc.getString("notify") ?: ""

                    tvWelcome.text = "Welcome, $email"
                    tvRole.text = "Role: $role"
                    tvRewardPoints.text = points.toString()
                    tvBadge.text = getBadge(points)

                    // Avatar letter
                    val letter = email.substring(0,1).uppercase()
                    tvAvatar.text = letter

                    // Random avatar color
                    val colors = listOf(
                        "#F44336",
                        "#E91E63",
                        "#9C27B0",
                        "#3F51B5",
                        "#2196F3",
                        "#009688",
                        "#4CAF50",
                        "#FF9800"
                    )

                    val randomColor = colors.random()
                    tvAvatar.background.setTint(Color.parseColor(randomColor))

                    if (!loginNotified) {
                        showNotification(
                            "HelpLink",
                            "You are logged in successfully."
                        )
                        loginNotified = true
                    }

                    if (notify == "approved") {

                        showNotification(
                            "Account Approved",
                            "Your account has been approved by admin."
                        )

                        db.collection("users")
                            .document(uid)
                            .update("notify", "")
                    }
                }
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

    private fun showNotification(title: String, message: String) {

        val channelId = "helplink_channel"

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "HelpLink Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getBadge(points: Long): String {

        return when {

            points >= 100 -> "Badge: Gold 🥇"

            points >= 50 -> "Badge: Silver 🥈"

            else -> "Badge: Bronze 🥉"
        }
    }
}