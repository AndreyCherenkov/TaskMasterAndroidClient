package ru.andreycherenkov.taskmaster.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.andreycherenkov.taskmaster.LocalRepository
import ru.andreycherenkov.taskmaster.api.TaskApi
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoCreateRequest
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoResponse
import ru.andreycherenkov.taskmaster.db.TaskRepository
import ru.andreycherenkov.taskmaster.api.dto.TaskUpdateDto

class TaskClient(private val localRepository: LocalRepository) {

    private val taskApi: TaskApi = RetrofitClient.taskApi

    suspend fun createTask(
        taskDtoCreateRequest: TaskDtoCreateRequest,
        taskRepository: TaskRepository
    ): TaskDtoResponse? {
        return withContext(Dispatchers.IO) {
            val response =
                taskApi.createTask(getToken(), taskDtoCreateRequest)
                    .execute()
            if (response.isSuccessful) {
                response.body()?.also { taskRepository.addTask(it) }
            } else {
                Log.e(
                    "createTask",
                    "Ошибка при создании задачи: ${response.code()} - ${response.body()}"
                )
                null
            }
        }
    }

    suspend fun getAllTasks(
        userId: String,
        taskRepository: TaskRepository
    ): List<TaskDtoResponse>? {
        return withContext(Dispatchers.IO) {
            val response = taskApi.getTasks(getToken(), userId).execute()
            if (response.isSuccessful) {
                val tasks = response.body()
                tasks?.forEach { task ->
                    if (taskRepository.getTask(task.taskId) == null) {
                        taskRepository.addTask(task)
                    } else {
                        taskRepository.updateTask(task)
                    }
                }
                tasks
            } else {
                Log.e(
                    "getAllTasks",
                    "Ошибка при получении задач: ${response.code()} - ${response.message()}"
                )
                null
            }
        }
    }

    suspend fun updateTask(
        updateDto: TaskUpdateDto,
        taskRepository: TaskRepository
    ): TaskDtoResponse? {
        return withContext(Dispatchers.IO) {
            val response = taskApi.updateTask(getToken(), updateDto).execute()
            if (response.isSuccessful) {
                response.body()?.also { taskRepository.updateTask(it) }
            } else {
                Log.e(
                    "updateTask",
                    "Ошибка при обновлении задачи: ${response.code()} - ${response.message()}"
                )
                null
            }
        }
    }

    suspend fun deleteTask(taskId: String, taskRepository: TaskRepository) {
        withContext(Dispatchers.IO) {
            val response = taskApi.deleteTask(getToken(), taskId).execute()
            if (response.isSuccessful) {
                taskRepository.deleteTask(taskId)
            } else {
                Log.e(
                    "deleteTask",
                    "Ошибка при удалении задачи: ${response.code()} - ${response.message()}"
                )
            }
        }
    }

    private suspend fun getToken() = localRepository.token.first().toString()
}
