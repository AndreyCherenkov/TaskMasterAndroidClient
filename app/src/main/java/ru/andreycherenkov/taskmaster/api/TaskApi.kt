package ru.andreycherenkov.taskmaster.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoCreateRequest
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoResponse
import ru.andreycherenkov.taskmaster.api.dto.TaskUpdateDto

interface TaskApi {

    companion object {
        private const val BASE_URL = "api/v1/tasks"
    }

    @GET(BASE_URL)
    fun getTasks(
        @Header("Authorization") authToken: String,
        @Header("User-Id") userId: String
    ): Call<List<TaskDtoResponse>>


    @GET("${BASE_URL}/{taskId}")
    fun getTask(
        @Header("Authorization") authToken: String,
        @Path("taskId") taskId: String
    ): Call<TaskDtoResponse>

    @POST(BASE_URL)
    fun createTask(
        @Header("Authorization") authToken: String,
        @Body taskDtoCreate: TaskDtoCreateRequest
    ): Call<TaskDtoResponse>

    @PUT(BASE_URL)
    fun updateTask(
        @Header("Authorization") authToken: String,
        @Body taskUpdateDto: TaskUpdateDto
    ): Call<TaskDtoResponse>

    @DELETE("${BASE_URL}/{taskId}")
    fun deleteTask(
        @Header("Authorization") authToken: String,
        @Path("taskId") taskId: String
    ): Call<Void>

}