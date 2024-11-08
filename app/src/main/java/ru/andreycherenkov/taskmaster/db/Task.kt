package ru.andreycherenkov.taskmaster.db

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID


data class Task(
    val id: UUID,
    val taskUUID: UUID,
    val userId: UUID,
    val title: String,
    val description: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val startDate: LocalDate,
    val dueDate: LocalDate,
    val updatedAt: LocalDateTime,
)