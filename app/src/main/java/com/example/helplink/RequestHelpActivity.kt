package com.example.helplink

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RequestHelpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var locationClient: FusedLocationProviderClient

    private var lat: Double? = null
    private var lng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_help)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        locationClient = LocationServices.getFusedLocationProviderClient(this)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitRequest)

        getCurrentLocation()

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val user = auth.currentUser ?: return@setOnClickListener

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (lat == null || lng == null) {
                Toast.makeText(this, "Waiting for GPS location...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 CREATE DOCUMENT WITH ID (IMPORTANT)
            val docRef = db.collection("help_requests").document()

            val requestData = hashMapOf(
                "id" to docRef.id, // ✅ ensures jobId works everywhere
                "title" to title,
                "description" to description,
                "requesterId" to user.uid,
                "requesterEmail" to (user.email ?: "Unknown User"), // ✅ FIX NULL EMAIL
                "status" to "open",
                "lat" to lat,
                "lng" to lng,
                "createdAt" to System.currentTimeMillis()
            )

            docRef.set(requestData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Task posted with location", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to post task", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 📍 Get real GPS
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lat = location.latitude
                lng = location.longitude
            }
        }
    }
}