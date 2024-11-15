package ru.andreycherenkov.taskmaster.api.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class UserDtoCreateResponse(
    @SerializedName("user_id") val userId: UUID,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("jwt_token") val jwtToken: String,
)
