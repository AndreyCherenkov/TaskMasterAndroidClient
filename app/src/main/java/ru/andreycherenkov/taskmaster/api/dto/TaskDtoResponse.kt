package ru.andreycherenkov.taskmaster.api.dto

import ru.andreycherenkov.taskmaster.db.TaskPriority
import ru.andreycherenkov.taskmaster.db.TaskStatus
import java.time.LocalDateTime
import java.util.UUID

data class TaskDtoResponse(
    val taskId: UUID,
    val userId: UUID,
    val title: String,
    val description: String,
    val priority: TaskPriority,
    val taskStatus: TaskStatus,
    val dueDate: LocalDateTime
)
