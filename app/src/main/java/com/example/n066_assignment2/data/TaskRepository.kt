package com.example.n066_assignment2.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository(
    databaseUrl: String = "https://fir-todoassignment-default-rtdb.asia-southeast1.firebasedatabase.app"
) {

    private val tasksRef: DatabaseReference by lazy {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (_: Exception) {
            // Already initialized or persistence could not be set
        }

        val database = try {
            FirebaseDatabase.getInstance(databaseUrl)
        } catch (_: Exception) {
            FirebaseDatabase.getInstance()
        }
        database.reference.child("tasks")
    }

    /**
     * Observes real-time updates of tasks using Kotlin Coroutines callbackFlow.
     * Automatically syncs changes across multiple clients in real time.
     */
    fun getTasks(): Flow<List<Task>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = snapshot.children.mapNotNull { child ->
                    val task = child.getValue(Task::class.java)
                    task?.copy(
                        id = child.key ?: task.id.ifBlank { "" }
                    )
                }
                trySend(tasks)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        tasksRef.addValueEventListener(listener)

        awaitClose {
            tasksRef.removeEventListener(listener)
        }
    }

    /**
     * Adds a new task to Firebase Realtime Database.
     */
    suspend fun addTask(description: String, priority: String) {
        val taskRef = tasksRef.push()
        val taskId = taskRef.key ?: ""
        val task = Task(
            id = taskId,
            description = description.trim(),
            priority = priority,
            completed = false,
            timestamp = System.currentTimeMillis()
        )
        taskRef.setValue(task).await()
    }

    /**
     * Updates an existing task's description, priority, or completion status.
     */
    suspend fun updateTask(task: Task) {
        if (task.id.isNotBlank()) {
            tasksRef.child(task.id).setValue(task).await()
        }
    }

    /**
     * Deletes a task by ID from Firebase Realtime Database.
     */
    suspend fun deleteTask(taskId: String) {
        if (taskId.isNotBlank()) {
            tasksRef.child(taskId).removeValue().await()
        }
    }

    /**
     * Toggles the completion status of a task.
     */
    suspend fun toggleTaskCompletion(task: Task) {
        if (task.id.isNotBlank()) {
            tasksRef.child(task.id).child("completed").setValue(!task.completed).await()
        }
    }
}
