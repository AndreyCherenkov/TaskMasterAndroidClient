package ru.andreycherenkov.taskmaster.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.andreycherenkov.taskmaster.LocalRepository
import ru.andreycherenkov.taskmaster.TaskMapper
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoCreateRequest
import ru.andreycherenkov.taskmaster.api.dto.TaskItemDto
import ru.andreycherenkov.taskmaster.db.TaskRepository
import ru.andreycherenkov.taskmaster.api.dto.TaskUpdateDto
import ru.andreycherenkov.taskmaster.network.TaskClient

class TaskViewModel(private val taskRepository: TaskRepository, localRepository: LocalRepository) : ViewModel() {

    private val taskClient = TaskClient(localRepository)

    private val _tasks = MutableLiveData<List<TaskItemDto>>()
    val tasks: LiveData<List<TaskItemDto>> get() = _tasks

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val taskMapper by lazy {
        TaskMapper()
    }

    fun loadTasks(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                taskClient.getAllTasks(userId, taskRepository)
                _tasks.value = taskRepository.getAllTasks()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при загрузке задач: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskClient.deleteTask(taskId, taskRepository)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении задачи: ${e.message}"
            }
        }
    }

    fun createTask(newTask: TaskDtoCreateRequest) {
        viewModelScope.launch {
            try {
                val createdTask = taskClient.createTask(newTask, taskRepository)
                if (createdTask != null) {
                    val newTaskItem = taskMapper.mapToTaskItemDto(createdTask)
                    val updatedTasks = _tasks.value.orEmpty() + newTaskItem
                    _tasks.value = updatedTasks
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при создании задачи: ${e.message}"
            }
        }
    }

    fun updateTask(updatedTask: TaskUpdateDto) {
        viewModelScope.launch {
            try {
                val response = taskClient.updateTask(updatedTask, taskRepository)
                if (response != null) {
                    val updatedTaskItem = taskMapper.mapToTaskItemDto(response)
                    Log.i("BEFORE UPDATE", _tasks.value.toString())
                    val updatedList = _tasks.value.orEmpty()
                        .map { task ->
                            if (task.taskUUID == updatedTaskItem.taskUUID) {
                                updatedTaskItem
                            } else {
                                task
                            }
                        }
                    _tasks.value = updatedList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении задачи: ${e.message}"
            }
        }
    }



}
