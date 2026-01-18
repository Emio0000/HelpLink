package com.example.helplink

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val tvChat = findViewById<TextView>(R.id.tvChat)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)

        val chatId = intent.getStringExtra("chatId") ?: return
        val uid = auth.currentUser?.uid ?: return

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val builder = StringBuilder()
                snapshot?.forEach {
                    val sender = it.getString("senderId")
                    val text = it.getString("text")
                    builder.append(
                        if (sender == uid) "You: $text\n"
                        else "Other: $text\n"
                    )
                }
                tvChat.text = builder.toString()
            }

        btnSend.setOnClickListener {
            val msg = etMessage.text.toString().trim()
            if (msg.isEmpty()) return@setOnClickListener

            val data = mapOf(
                "senderId" to uid,
                "text" to msg,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(data)

            db.collection("chats")
                .document(chatId)
                .update(
                    "lastMessage", msg,
                    "updatedAt", System.currentTimeMillis()
                )

            etMessage.text.clear()
        }
    }
}
