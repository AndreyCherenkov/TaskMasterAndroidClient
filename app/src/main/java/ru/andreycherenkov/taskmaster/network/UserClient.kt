package ru.andreycherenkov.taskmaster.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.andreycherenkov.taskmaster.LocalRepository
import ru.andreycherenkov.taskmaster.api.UserApi
import ru.andreycherenkov.taskmaster.api.dto.AuthResponse
import ru.andreycherenkov.taskmaster.api.dto.UserCreateDto
import ru.andreycherenkov.taskmaster.api.dto.UserDtoCreateResponse
import ru.andreycherenkov.taskmaster.api.dto.UserLoginDto

class UserClient(localRepository: LocalRepository) {

    private val userApi: UserApi = RetrofitClient.userApi

    suspend fun createUser(userCreateDto: UserCreateDto): UserDtoCreateResponse? {
        return withContext(Dispatchers.IO) {
            val response = userApi.createUser(userCreateDto).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(
                    "createUser",
                    "Ошибка при регистрации пользователя: ${response.code()} - ${response.message()}"
                )
                null
            }
        }
    }

    suspend fun login(userLoginDto: UserLoginDto): AuthResponse? {
        return withContext(Dispatchers.IO) {
            val response = userApi.login(userLoginDto).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(
                    "login",
                    "Ошибка при входе пользователя: ${response.code()} - ${response.message()}"
                )
                null
            }
        }
    }
}

