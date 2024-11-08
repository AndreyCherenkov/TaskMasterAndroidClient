package ru.andreycherenkov.taskmaster

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.andreycherenkov.taskmaster.api.TaskApi
import ru.andreycherenkov.taskmaster.api.UserApi

object RetrofitClient {

    private const val BASE_URL = "http://192.168.0.104:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val taskApi: TaskApi by lazy {
        retrofit.create(TaskApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }
}