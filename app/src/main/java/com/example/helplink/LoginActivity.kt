package com.example.helplink

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        loginBtn.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = auth.currentUser!!.uid

                    db.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { doc ->

                            if (!doc.exists()) {
                                Toast.makeText(
                                    this,
                                    "No user record found",
                                    Toast.LENGTH_LONG
                                ).show()
                                FirebaseAuth.getInstance().signOut()
                                return@addOnSuccessListener
                            }

                            val role = doc.getString("role") ?: ""
                            val status = doc.getString("status") ?: ""

                            if (role == "admin") {

                                startActivity(
                                    Intent(this, AdminDashboardActivity::class.java)
                                )

                            } else if (status == "active") {

                                startActivity(
                                    Intent(this, HomeActivity::class.java)
                                )

                            } else {

                                startActivity(
                                    Intent(this, GuestWaitingActivity::class.java)
                                )
                            }

                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
