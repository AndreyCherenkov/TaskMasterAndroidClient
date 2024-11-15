package ru.andreycherenkov.taskmaster.api.dto

import com.google.gson.annotations.SerializedName
import ru.andreycherenkov.taskmaster.db.TaskPriority
import ru.andreycherenkov.taskmaster.db.TaskStatus
import java.util.UUID

data class TaskUpdateDto(
    @SerializedName("task_id") val taskId: UUID,
    @SerializedName("user_id") val userId: UUID,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("priority") val priority: TaskPriority?,
    @SerializedName("task_status") val status: TaskStatus?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("due_date") val dueDate: String?,
)
