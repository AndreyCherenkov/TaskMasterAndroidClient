package ru.andreycherenkov.taskmaster.viewModel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.andreycherenkov.taskmaster.LocalRepository
import ru.andreycherenkov.taskmaster.db.TaskRepository
import ru.andreycherenkov.taskmaster.viewModel.TaskViewModel

class TaskViewModelFactory(private val taskRepository: TaskRepository, private val localRepository: LocalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskRepository, localRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
