package com.example.helplink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatsActivity : AppCompatActivity() {

    private val chatList = mutableListOf<ChatRoom>()
    private lateinit var adapter: ChatsAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        val rv = findViewById<RecyclerView>(R.id.rvChats)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = ChatsAdapter(chatList) { chat ->

            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chat.jobId)
            startActivity(intent)

        }

        rv.adapter = adapter

        loadChats()
    }

    private fun loadChats() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("chats")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, _ ->

                chatList.clear()

                snapshot?.forEach {
                    val chat = it.toObject(ChatRoom::class.java)
                    chatList.add(chat)
                }

                adapter.notifyDataSetChanged()
            }
    }
}

