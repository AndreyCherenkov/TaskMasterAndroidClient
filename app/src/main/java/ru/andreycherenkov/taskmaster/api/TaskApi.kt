package ru.andreycherenkov.taskmaster.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import ru.andreycherenkov.taskmaster.UserResponse
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoCreateRequest
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoResponse

interface TaskApi {

    companion object {
        private const val BASE_URL = "api/v1/tasks"
    }

    @GET("api/v1/users/test")
    fun test(): Call<UserResponse>

    @GET(BASE_URL)
    fun getTasks(@Header("User-Id") userId: String): Call<List<TaskDtoResponse>>

    @GET("${BASE_URL}/{taskId}")
    fun getTask(@Path("taskId") taskId: String): Call<TaskDtoResponse>

    @POST(BASE_URL)
    fun createTask(@Body taskDtoCreate: TaskDtoCreateRequest): Call<TaskDtoResponse>

    @DELETE("${BASE_URL}/{taskId}")
    fun deleteTask(@Path("taskId") taskId: String): Call<Void>

}