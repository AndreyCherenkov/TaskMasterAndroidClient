package ru.andreycherenkov.taskmaster.db

enum class TaskPriority(val priority: String) {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    UNDEFINED("UNDEFINED");

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