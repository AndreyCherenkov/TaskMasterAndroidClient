package ru.andreycherenkov.taskmaster.api.dto

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class AuthResponse(
    @SerializedName("jwt_token") val jwtToken: String,
    @SerializedName("user_id") val userId: UUID
)
