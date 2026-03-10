package com.example.helplink

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val messageList = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val rv = findViewById<RecyclerView>(R.id.rvMessages)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)

        val chatId = intent.getStringExtra("chatId") ?: return
        val uid = auth.currentUser?.uid ?: return

        adapter = MessageAdapter(messageList)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // Listen for messages (real-time)
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->

                messageList.clear()

                snapshot?.forEach {
                    val msg = it.toObject(Message::class.java)
                    messageList.add(msg)
                }

                adapter.notifyDataSetChanged()

                if (messageList.isNotEmpty()) {
                    rv.scrollToPosition(messageList.size - 1)
                }
            }

        // Send message
        btnSend.setOnClickListener {

            val text = etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val messageData = hashMapOf(
                "senderId" to uid,
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener {

                    db.collection("chats")
                        .document(chatId)
                        .update(
                            mapOf(
                                "lastMessage" to text,
                                "lastSenderId" to uid,
                                "updatedAt" to System.currentTimeMillis()
                            )
                        )
                }

            etMessage.text.clear()
        }
        }
    }
