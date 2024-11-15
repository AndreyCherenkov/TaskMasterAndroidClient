package ru.andreycherenkov.taskmaster.api.dto

import com.google.gson.annotations.SerializedName

data class UserCreateDto(
    @SerializedName("username") private val username: String,
    @SerializedName("email") private val email: String,
    @SerializedName("password") private val password: String,
    @SerializedName("confirmed_password") private val confirmedPassword: String,
)
