package com.example.helplink

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapsActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var userMarker: Marker? = null   // 🔥 IMPORTANT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_maps)

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        showUserLocation()
        loadNearbyTasks()
    }

    // 📍 Live GPS (DO NOT clear overlays)
    private fun showUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3000
        ).build()

        locationClient.requestLocationUpdates(
            request,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    val point = GeoPoint(loc.latitude, loc.longitude)

                    map.controller.setZoom(17.0)
                    map.controller.setCenter(point)

                    if (userMarker == null) {
                        userMarker = Marker(map).apply {
                            position = point
                            title = "You are here"
                        }
                        map.overlays.add(userMarker)
                    } else {
                        userMarker!!.position = point
                    }

                    map.invalidate()
                }
            },
            mainLooper
        )
    }

    // 🧭 Load tasks ONCE and keep them
    private fun loadNearbyTasks() {
        db.collection("help_requests")
            .whereEqualTo("status", "open")
            .addSnapshotListener { docs, _ ->

                if (docs == null) return@addSnapshotListener

                // Remove ONLY task markers
                map.overlays.removeAll {
                    it is Marker && it != userMarker
                }

                for (doc in docs) {
                    val lat = doc.getDouble("lat") ?: continue
                    val lng = doc.getDouble("lng") ?: continue

                    val marker = Marker(map)
                    marker.position = GeoPoint(lat, lng)
                    marker.title = doc.getString("title")
                    marker.subDescription =
                        "By: ${doc.getString("requesterEmail")}"

                    marker.setOnMarkerClickListener { _, _ ->
                        showAcceptDialog(
                            jobId = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            requesterId = doc.getString("requesterId") ?: "",
                            requesterEmail = doc.getString("requesterEmail") ?: ""
                        )
                        true
                    }

                    map.overlays.add(marker)
                }

                map.invalidate()
            }
    }

    // 💬 Accept dialog
    private fun showAcceptDialog(
        jobId: String,
        title: String,
        description: String,
        requesterId: String,
        requesterEmail: String
    ) {
        val user = auth.currentUser ?: return

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(
                "Description:\n$description\n\nRequester:\n$requesterEmail"
            )
            .setPositiveButton("Accept Task") { _, _ ->

                // 1️⃣ Update job
                db.collection("help_requests")
                    .document(jobId)
                    .update(
                        mapOf(
                            "status" to "accepted",
                            "helperId" to user.uid,
                            "helperEmail" to user.email
                        )
                    )

                // 2️⃣ Create chat
                val chatMap = hashMapOf(
                    "jobId" to jobId,
                    "requesterId" to requesterId,
                    "helperId" to user.uid,
                    "participants" to listOf(requesterId, user.uid),
                    "lastMessage" to "",
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                db.collection("chats")
                    .document(jobId)
                    .set(chatMap)

                // 3️⃣ Go to chats
                startActivity(Intent(this, ChatsActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
