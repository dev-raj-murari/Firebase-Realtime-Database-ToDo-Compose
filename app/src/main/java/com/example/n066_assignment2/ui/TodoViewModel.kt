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
    val error: String? = null
)

class TodoViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTasks()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load tasks"
                        )
                    }
                }
                .collect { tasks ->
                    // Priority management: sorted dynamically by priority (High -> Medium -> Low)
                    val sortedTasks = tasks.sortedBy {
                        when (it.priority) {
                            "High" -> 0
                            "Medium" -> 1
                            "Low" -> 2
                            else -> 3
                        }
                    }

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
        viewModelScope.launch {
            runCatching {
                repository.deleteTask(taskId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(error = error.message ?: "Failed to delete task")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
