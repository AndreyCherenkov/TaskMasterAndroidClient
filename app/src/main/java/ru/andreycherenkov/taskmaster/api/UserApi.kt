package ru.andreycherenkov.taskmaster.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ru.andreycherenkov.taskmaster.api.dto.AuthResponse
import ru.andreycherenkov.taskmaster.api.dto.UserDtoCreateResponse
import ru.andreycherenkov.taskmaster.api.dto.UserCreateDto
import ru.andreycherenkov.taskmaster.api.dto.UserLoginDto

interface UserApi {

    @GET("api/v1/users/test")
    fun test(): Call<String>

    @POST("api/v1/users/register")
    fun createUser(@Body userCreateDto: UserCreateDto): Call<UserDtoCreateResponse>

    @POST("api/v1/users/login")
    fun login(@Body userLoginDto: UserLoginDto): Call<AuthResponse>

//    @GET("api/v1/users/{userId}")
//    fun getUser(@Path("userId") userId: String): Call<UserDataResponse>
}
