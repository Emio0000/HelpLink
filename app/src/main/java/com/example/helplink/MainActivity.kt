package com.example.helplink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Always redirect to Login
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
