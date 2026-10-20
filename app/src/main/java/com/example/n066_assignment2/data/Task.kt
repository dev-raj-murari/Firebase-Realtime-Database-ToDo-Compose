package com.example.n066_assignment2.data

data class Task(
    val id: String = "",
    val description: String = "",
    val priority: String = "Medium",
    val completed: Boolean = false,
    val timestamp: Long = 0L
)
