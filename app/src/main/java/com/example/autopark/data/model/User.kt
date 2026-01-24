package com.example.autopark.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val role: String = "", // "admin" or "driver"
    val name: String = "",
    val phoneNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)