package ru.andreycherenkov.taskmaster

import ru.andreycherenkov.taskmaster.api.dto.TaskDtoResponse
import ru.andreycherenkov.taskmaster.api.dto.TaskItemDto

class TaskMapper {
    fun mapToTaskItemDto(taskDtoResponse: TaskDtoResponse): TaskItemDto {
        return TaskItemDto(
            taskUUID = taskDtoResponse.taskId,
            title = taskDtoResponse.title,
            description = taskDtoResponse.description,
            status = taskDtoResponse.taskStatus,
            priority = taskDtoResponse.priority,
            startDate = taskDtoResponse.startDate,
            dueDate = taskDtoResponse.dueDate ?: ""
        )
    }

}