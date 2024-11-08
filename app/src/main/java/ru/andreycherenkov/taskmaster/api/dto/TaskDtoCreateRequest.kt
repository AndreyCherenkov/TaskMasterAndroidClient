package ru.andreycherenkov.taskmaster.api.dto

import com.google.gson.annotations.SerializedName
import ru.andreycherenkov.taskmaster.db.TaskPriority
import ru.andreycherenkov.taskmaster.db.TaskStatus
import java.time.LocalDate
import java.util.UUID

data class TaskDtoCreateRequest(
    @SerializedName("user_id") val userId: UUID,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("priority") val priority: TaskPriority,
    @SerializedName("task_status") val taskStatus: TaskStatus,
    @SerializedName("due_date") val dueDate: String
)
