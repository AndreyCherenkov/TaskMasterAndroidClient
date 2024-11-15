package ru.andreycherenkov.taskmaster.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.andreycherenkov.taskmaster.LocalRepository
import ru.andreycherenkov.taskmaster.api.dto.AuthResult
import ru.andreycherenkov.taskmaster.api.dto.UserCreateDto
import ru.andreycherenkov.taskmaster.api.dto.UserLoginDto
import ru.andreycherenkov.taskmaster.network.UserClient

class UserViewModel(private val localRepository: LocalRepository) : ViewModel() {
    private val userClient = UserClient(localRepository)

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> get() = _authResult

    fun createUser(userCreateDto: UserCreateDto) {
        viewModelScope.launch {
            try {
                val response = userClient.createUser(userCreateDto)
                response?.let {
                    localRepository.saveToken(it.jwtToken)
                    localRepository.saveUserId(it.userId.toString())
                    _authResult.postValue(
                        AuthResult(
                            isSuccessful = true,
                            token = it.jwtToken
                        )
                    )
                } ?: run {
                    _authResult.postValue(
                        AuthResult(
                            isSuccessful = false,
                            errorMessage = "Пользователь не создан."
                        )
                    )
                }
            } catch (e: Exception) {
                _authResult.postValue(
                    AuthResult(
                        isSuccessful = false,
                        errorMessage = e.message ?: "Неизвестная ошибка"
                    )
                )
            }
        }
    }

    fun login(loginDto: UserLoginDto) {
        viewModelScope.launch {
            try {
                val response = userClient.login(loginDto)
                response?.let {
                    localRepository.saveToken(it.jwtToken)
                    localRepository.saveUserId(it.userId.toString())
                    _authResult.postValue(
                        AuthResult(
                            isSuccessful = true,
                            token = it.jwtToken
                        )
                    )
                } ?: run {
                    _authResult.postValue(
                        AuthResult(
                            isSuccessful = false,
                            errorMessage = "Неверный логин или пароль."
                        )
                    )
                }
            } catch (e: Exception) {
                _authResult.postValue(
                    AuthResult(
                        isSuccessful = false,
                        errorMessage = "Ошибка сервера, попробуйте позже"
                    )
                )
            }
        }
    }
}
