package ru.andreycherenkov.taskmaster.db

enum class TaskPriority(val priority: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    companion object {
        fun fromString(value: String): TaskPriority {
            return when (value) {
                "Низкий" -> LOW
                "Средний" -> MEDIUM
                "Высокий" -> HIGH
                else -> MEDIUM
            }
        }
    }
}