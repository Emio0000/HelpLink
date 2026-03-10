package com.example.helplink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    fun setupBottomNav(selectedItem: Int) {

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNav.selectedItemId = selectedItem

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

                R.id.nav_jobs -> {
                    startActivity(Intent(this, MyJobsActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}