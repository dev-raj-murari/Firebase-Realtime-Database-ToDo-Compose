package com.example.n066_assignment2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.n066_assignment2.data.Task
import com.example.n066_assignment2.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodoUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedFilter: String = "All",
    val searchQuery: String = "",
    val recentlyDeletedTask: Task? = null
) {
    /**
     * Filtered tasks based on search query and selected priority filter.
     */
    val displayTasks: List<Task>
        get() {
            return tasks
                .filter { task ->
                    val matchesFilter = when (selectedFilter) {
                        "High" -> task.priority.equals("High", ignoreCase = true)
                        "Medium" -> task.priority.equals("Medium", ignoreCase = true)
                        "Low" -> task.priority.equals("Low", ignoreCase = true)
                        else -> true
                    }
                    val matchesSearch = searchQuery.isBlank() ||
                            task.description.contains(searchQuery, ignoreCase = true)
                    matchesFilter && matchesSearch
                }
        }

    val highPriorityCount: Int get() = tasks.count { it.priority.equals("High", ignoreCase = true) }
    val completedCount: Int get() = tasks.count { it.completed }
    val totalCount: Int get() = tasks.size
}

class TodoViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            repository.getTasks()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load tasks from Firebase"
                        )
                    }
                }
                .collect { tasks ->
                    // Dynamic sorting by Priority: High (0) -> Medium (1) -> Low (2)
                    // Secondary sorting: incomplete before completed, then timestamp
                    val sortedTasks = tasks.sortedWith(
                        compareBy<Task> { task ->
                            if (task.completed) 1 else 0
                        }.thenBy { task ->
                            when (task.priority.lowercase()) {
                                "high" -> 0
                                "medium" -> 1
                                "low" -> 2
                                else -> 3
                            }
                        }.thenByDescending { task ->
                            task.timestamp
                        }
                    )

                    _uiState.update {
                        it.copy(
                            tasks = sortedTasks,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun addTask(description: String, priority: String) {
        if (description.isBlank()) {
            _uiState.update {
                it.copy(error = "Task description cannot be empty")
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.addTask(description.trim(), priority)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(error = error.message ?: "Failed to add task")
                }
            }
        }
    }

    fun updateTask(task: Task) {
        if (task.description.isBlank()) {
            _uiState.update {
                it.copy(error = "Task description cannot be empty")
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.updateTask(task)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(error = error.message ?: "Failed to update task")
                }
            }
        }
    }

    fun deleteTask(taskId: String) {
        val taskToDelete = _uiState.value.tasks.find { it.id == taskId }

        viewModelScope.launch {
            runCatching {
                repository.deleteTask(taskId)
                _uiState.update { it.copy(recentlyDeletedTask = taskToDelete) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(error = error.message ?: "Failed to delete task")
                }
            }
        }
    }

    fun undoDelete() {
        val task = _uiState.value.recentlyDeletedTask ?: return
        viewModelScope.launch {
            runCatching {
                repository.addTask(task.description, task.priority)
                _uiState.update { it.copy(recentlyDeletedTask = null) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(error = error.message ?: "Failed to restore task")
                }
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            runCatching {
                repository.toggleTaskCompletion(task)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(error = error.message ?: "Failed to update task status")
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
