package com.example.helplink

data class ChatRoom(
    var jobId: String = "",
    var requesterId: String = "",
    var requesterEmail: String = "",
    var helperId: String = "",
    var helperEmail: String = "",
    var lastMessage: String = ""
) {

    fun getOtherUserEmail(currentUserId: String): String {
        return if (currentUserId == requesterId) helperEmail else requesterEmail
    }

    fun getOtherUserId(currentUserId: String): String {
        return if (currentUserId == requesterId) helperId else requesterId
    }
}
