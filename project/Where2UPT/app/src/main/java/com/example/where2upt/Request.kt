package com.example.where2upt

data class Request(
    val id: String = "",
    val type: String = "", // "student->staff" sau "staff->admin"
    val fromUserId: String = "",
    val fromRole: String = "",
    val toUserId: String = "",   // staff sau admin (uid)
    val toRole: String = "",
    val requesterFacultyId: String = "",
    val targetRoomId: String = "",
    val message: String = "",
    val status: String = "pending",
    val createdAt: Long = 0L,
    val decidedAt: Long = 0L
)