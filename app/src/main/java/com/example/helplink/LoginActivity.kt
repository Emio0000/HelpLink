package com.example.helplink

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val goToSignup = findViewById<TextView>(R.id.goToSignup)

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        loginBtn.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    // ✅ SHOW NOTIFICATION ONLY HERE
                    showLoginNotification()

                    Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()

                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    db.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { doc ->

                            val role = doc.getString("role") ?: ""
                            val status = doc.getString("status") ?: ""

                            if (role == "admin") {
                                startActivity(Intent(this, AdminDashboardActivity::class.java))
                            } else if (status == "active") {
                                startActivity(Intent(this, HomeActivity::class.java))
                            } else {
                                startActivity(Intent(this, GuestWaitingActivity::class.java))
                            }

                            finish()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Auth Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    // 🔔 LOGIN NOTIFICATION
    private fun showLoginNotification() {

        val channelId = "login_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Login Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Login Successful")
            .setContentText("Welcome back to HelpLink!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val manager = NotificationManagerCompat.from(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(1, builder.build())
        }
    }
}