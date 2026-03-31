package com.example.helplink

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
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

        // 🔥 Get location when page opens
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
                Toast.makeText(this, "Getting GPS location... try again", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
                return@setOnClickListener
            }

            val docRef = db.collection("help_requests").document()

            val requestData = hashMapOf(
                "id" to docRef.id,
                "title" to title,
                "description" to description,
                "requesterId" to user.uid,
                "requesterEmail" to (user.email ?: "Unknown User"),
                "status" to "open",
                "lat" to lat,
                "lng" to lng,
                "createdAt" to System.currentTimeMillis()
            )

            docRef.set(requestData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Task posted successfully ✅", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to post task", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 📍 FINAL LOCATION FUNCTION (REAL + MOCK STABLE)
    private fun getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        val request = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            // ❌ REMOVED numUpdates = 1 (important fix)
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {

                val location = result.lastLocation

                if (location != null) {
                    lat = location.latitude
                    lng = location.longitude

                    // 🔥 Detect mock vs real
                    if (location.isFromMockProvider) {
                        Toast.makeText(
                            this@RequestHelpActivity,
                            "Using Mock Location (Demo Mode)",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@RequestHelpActivity,
                            "Using Real GPS",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // 🔥 IMPORTANT: stop updates after getting location
                    locationClient.removeLocationUpdates(this)
                }
            }
        }

        locationClient.requestLocationUpdates(request, callback, mainLooper)
    }

    // 🔐 Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }
}