package ru.andreycherenkov.taskmaster.api

import retrofit2.Call
import retrofit2.http.GET
import ru.andreycherenkov.taskmaster.UserResponse

interface UserApi {

    @GET("api/v1/users/test")
    fun test(): Call<String>

//    @POST("api/v1/users/register")
//    fun createUser(@Body userRegisterDto: UserRegisterDto): Call<Any>
//
//    @POST("api/v1/users/login")
//    fun login(@Body userLoginDto: UserLoginDto): Call<Any>
//
//    @GET("api/v1/users/{userId}")
//    fun getUser(@Path("userId") userId: String): Call<UserDataResponse>
}
