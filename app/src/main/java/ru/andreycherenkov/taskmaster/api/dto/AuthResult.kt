package ru.andreycherenkov.taskmaster.api.dto

data class AuthResult(
    val isSuccessful: Boolean,
    val errorMessage: String? = null,
    val token: String? = null
)
