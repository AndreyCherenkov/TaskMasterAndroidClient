package ru.andreycherenkov.taskmaster.api.dto

import ru.andreycherenkov.taskmaster.db.TaskPriority
import ru.andreycherenkov.taskmaster.db.TaskStatus
import java.util.UUID

data class TaskItemDto(
    val taskUUID: UUID?,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val startDate: String,
    val dueDate: String
)