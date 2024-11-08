package ru.andreycherenkov.taskmaster.db

enum class TaskStatus(val status: String) {
    NEW("Новая"),
    IN_PROGRESS("В процессе"),
    DONE("Завершена");

    companion object {
        fun fromString(value: String): TaskStatus {
            return when (value) {
                "Новая" -> NEW
                "В процессе" -> IN_PROGRESS
                "Завершена" -> DONE
                else -> NEW
            }
        }
    }
}