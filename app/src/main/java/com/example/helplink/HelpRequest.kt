package com.example.helplink

data class HelpRequest(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var requesterId: String = "",      // ✅ IMPORTANT
    var requesterEmail: String = "",
    var status: String = "",
    var helperId: String = "",
    var helperEmail: String = ""
)
