package com.example.n066_assignment2.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository {

    private val database = try {
        FirebaseDatabase.getInstance("https://devraj-todo-default-rtdb.firebaseio.com")
    } catch (_: Exception) {
        try {
            FirebaseDatabase.getInstance("https://devraj-todo-default-rtdb.asia-southeast1.firebasedatabase.app")
        } catch (_: Exception) {
            FirebaseDatabase.getInstance()
        }
    }

    private val tasksRef = database.reference.child("tasks")

    fun getTasks() = callbackFlow<List<Task>> {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = snapshot.children.mapNotNull { child ->
                    child.getValue(Task::class.java)?.copy(
                        id = child.key ?: ""
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

    suspend fun addTask(description: String, priority: String) {
        val taskRef = tasksRef.push()
        val task = Task(
            id = taskRef.key ?: "",
            description = description.trim(),
            priority = priority
        )
        taskRef.setValue(task).await()
    }

    suspend fun updateTask(task: Task) {
        tasksRef.child(task.id).setValue(task).await()
    }

    suspend fun deleteTask(taskId: String) {
        tasksRef.child(taskId).removeValue().await()
    }
}
