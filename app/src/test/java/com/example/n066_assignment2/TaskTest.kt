package com.example.n066_assignment2

import com.example.n066_assignment2.data.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TaskTest {

    @Test
    fun testTaskDefaultValues() {
        val task = Task()
        assertEquals("", task.id)
        assertEquals("", task.description)
        assertEquals("Medium", task.priority)
        assertFalse(task.completed)
    }

    @Test
    fun testPrioritySortingOrder() {
        val tasks = listOf(
            Task(id = "1", description = "Low task", priority = "Low"),
            Task(id = "2", description = "High task", priority = "High"),
            Task(id = "3", description = "Medium task", priority = "Medium")
        )

        val sorted = tasks.sortedBy {
            when (it.priority.lowercase()) {
                "high" -> 0
                "medium" -> 1
                "low" -> 2
                else -> 3
            }
        }

        assertEquals("High", sorted[0].priority)
        assertEquals("Medium", sorted[1].priority)
        assertEquals("Low", sorted[2].priority)
    }
}
