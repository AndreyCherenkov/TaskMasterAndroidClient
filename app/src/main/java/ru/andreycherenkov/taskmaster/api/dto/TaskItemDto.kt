package ru.andreycherenkov.taskmaster.api.dto

import ru.andreycherenkov.taskmaster.db.TaskPriority
import ru.andreycherenkov.taskmaster.db.TaskStatus

data class TaskItemDto(
    val taskId: Long?,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val startDate: String,
    val dueDate: String
)