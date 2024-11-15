package ru.andreycherenkov.taskmaster.db

import java.util.UUID


data class Task(
    val taskUUID: UUID,
    val userId: UUID,
    val title: String,
    val description: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val startDate: String?,
    val dueDate: String?,
    val updatedAt: String?,
)