package ru.andreycherenkov.taskmaster.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.andreycherenkov.taskmaster.RetrofitClient
import ru.andreycherenkov.taskmaster.api.TaskApi
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoCreateRequest
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoResponse
import ru.andreycherenkov.taskmaster.api.dto.TaskItemDto
import ru.andreycherenkov.taskmaster.db.TaskRepository
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.ceil

class TaskClient {

    private val taskApi: TaskApi = RetrofitClient.taskApi

    fun getAllTasks(userId: String, taskRepository: TaskRepository, context: Context) {
        taskApi.getTasks(userId).enqueue(object : Callback<List<TaskDtoResponse>> {
            override fun onResponse(
                call: Call<List<TaskDtoResponse>>,
                response: Response<List<TaskDtoResponse>>
            ) {
                if (response.isSuccessful) {
                    val tasks: List<TaskDtoResponse>? = response.body()
                    tasks?.let {
                        for (task in it) {
                            if (taskRepository.getTask(task.taskId) == null) {
                                taskRepository.addTask(task)
                            } else {
                                taskRepository.updateTask(task)
                            }
                        }
                    }
                } else {
                    Log.e(
                        "getAllTasks",
                        "Ошибка при получении задач: ${response.code()} - ${response.message()}"
                    )
                    Toast.makeText(
                        context,
                        "Ошибка при получении задач. Попробуйте позже.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<TaskDtoResponse>>, t: Throwable) {
                Log.e("getAllTasks", "Ошибка сети: ${t.message}", t)
                Toast.makeText(
                    context,
                    "Проблема с сетью. Проверьте подключение.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun deleteTask(taskId: String, taskRepository: TaskRepository, context: Context) {
        taskApi.deleteTask(taskId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    taskRepository.deleteTask(taskId)
                } else {
                    Log.e(
                        "deleteTask",
                        "Ошибка при удалении задачи: ${response.code()} - ${response.message()}"
                    )
                    Toast.makeText(
                        context,
                        "Ошибка при удалении задачи. Попробуйте позже.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("deleteTask", "Ошибка сети: ${t.message}", t)
                Toast.makeText(
                    context,
                    "Проблема с сетью. Проверьте подключение.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun addTask(
        taskDtoCreateRequest: TaskDtoCreateRequest,
        taskRepository: TaskRepository
    ) {
        taskApi.createTask(taskDtoCreateRequest).enqueue(object : Callback<TaskDtoResponse> {
            override fun onResponse(
                call: Call<TaskDtoResponse>,
                response: Response<TaskDtoResponse>
            ) {
                if (response.isSuccessful) {
                    val createdTask = response.body()
                    if (createdTask != null) {
                        val id = taskRepository.addTask(createdTask)
                        val taskItem = with(createdTask) {
                            TaskItemDto(
                                taskId = id,
                                taskUUID =  createdTask.taskId,
                                title = title,
                                description = description,
                                status = taskStatus,
                                priority = priority,
                                startDate = startDate,
                                dueDate = dueDate.toString()
                            )
                        }
                    }
                    Logger.getGlobal().log(Level.INFO, "RESPONSE SUCCESSFUL")
                }
            }

            override fun onFailure(call: Call<TaskDtoResponse>, t: Throwable) {
                Logger.getGlobal().log(Level.INFO, "RESPONSE FAILED: ${t.message}")
            }
        })

    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork?.let {
                connectivityManager.getNetworkCapabilities(it)
            }
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}
