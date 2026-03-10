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

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.signupEmail)
        val passwordInput = findViewById<EditText>(R.id.signupPassword)
        val fullNameInput = findViewById<EditText>(R.id.fullName)
        val streetInput = findViewById<EditText>(R.id.street)
        val houseNumberInput = findViewById<EditText>(R.id.houseNumber)
        val residentTypeInput = findViewById<EditText>(R.id.residentType)
        val residentCodeInput = findViewById<EditText>(R.id.residentCode)

        val signupBtn = findViewById<Button>(R.id.signupBtn)
        val backToLogin = findViewById<TextView>(R.id.backToLogin)

        signupBtn.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val fullName = fullNameInput.text.toString().trim()
            val street = streetInput.text.toString().trim()
            val houseNumber = houseNumberInput.text.toString().trim()
            val residentType = residentTypeInput.text.toString().trim()
            val residentCode = residentCodeInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() ||
                street.isEmpty() || houseNumber.isEmpty() ||
                residentType.isEmpty() || residentCode.isEmpty()) {

                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(
                    this,
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->

                    val uid = result.user!!.uid
                    val userEmail = result.user!!.email!!

                    val userMap = hashMapOf(
                        "name" to fullName,
                        "email" to userEmail,
                        "street" to street,
                        "houseNumber" to houseNumber,
                        "residentType" to residentType,
                        "residentCode" to residentCode,
                        "role" to "user",
                        "status" to "pending",
                        "rewardPoints" to 0,
                        "completedTasks" to 0,
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("users")
                        .document(uid)
                        .set(userMap)
                        .addOnSuccessListener {

                            Toast.makeText(
                                this,
                                "Account created. Waiting for admin approval.",
                                Toast.LENGTH_LONG
                            ).show()

                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }

        backToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}