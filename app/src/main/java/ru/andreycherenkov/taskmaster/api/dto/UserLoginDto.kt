package ru.andreycherenkov.taskmaster.api.dto

import com.google.gson.annotations.SerializedName

data class UserLoginDto(

    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String

)
