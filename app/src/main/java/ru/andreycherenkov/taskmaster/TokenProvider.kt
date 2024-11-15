package ru.andreycherenkov.taskmaster

interface TokenProvider {
    fun getToken(): String?
}
