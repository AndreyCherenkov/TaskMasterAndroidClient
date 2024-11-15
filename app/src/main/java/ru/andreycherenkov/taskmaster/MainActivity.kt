package ru.andreycherenkov.taskmaster

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoCreateRequest
import ru.andreycherenkov.taskmaster.db.TaskRepository
import ru.andreycherenkov.taskmaster.api.dto.TaskItemDto
import ru.andreycherenkov.taskmaster.api.dto.TaskUpdateDto
import ru.andreycherenkov.taskmaster.db.Task
import ru.andreycherenkov.taskmaster.db.TaskPriority
import ru.andreycherenkov.taskmaster.db.TaskStatus
import ru.andreycherenkov.taskmaster.viewModel.TaskViewModel
import ru.andreycherenkov.taskmaster.viewModel.factory.TaskViewModelFactory
import java.time.LocalDate
import java.util.UUID

class MainActivity : AppCompatActivity(), TaskAdapter.OnTaskClickListener {

    private lateinit var taskList: MutableList<TaskItemDto>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var navigationView: NavigationView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var createTaskButton: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var localRepository: LocalRepository
    private val taskRepository by lazy { TaskRepository(this) }
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            taskRepository,
            LocalRepository(this)
        )
    }

    private var userId: UUID? = null
    private var isLoggedIn: Boolean = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        setupRecyclerView()
        setupObservers()

        if (!isNetworkAvailable(this)) {
            lifecycleScope.launch {
                val localTasks = taskRepository.getAllTasks()
                if (localTasks.isNotEmpty()) {
                    taskList = localTasks
                    taskAdapter.updateTasks(localTasks)
                }
            }
        }

        localRepository = LocalRepository(this)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            userId?.let {
                taskViewModel.loadTasks(userId = it.toString())
            }
            swipeRefreshLayout.isRefreshing = false
        }

        val id = runBlocking {
            if (localRepository.userId.first() != null) {
                UUID.fromString(localRepository.userId.first().toString())
            } else {
                null
            }
        }

        userId = id

        userId?.let {
            isLoggedIn = true
            taskViewModel.loadTasks(userId = it.toString())
        }

        setupMenu(navigationView)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {}
                R.id.nav_register -> {
                    val intent = Intent(this, RegistrationActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_login -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        createTaskButton.setOnClickListener {
            showCreateTaskDialog()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(mutableListOf(), this, this)
        recyclerView.adapter = taskAdapter
    }

    private fun setupObservers() {
        taskViewModel.tasks.observe(this) { tasks ->
            tasks?.let {
                taskList = it.toMutableList()
                taskAdapter.updateTasks(taskList)
            }
        }

        taskViewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        taskViewModel.isLoading.observe(this) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        }


    }

    override fun onTaskLongClick(task: TaskItemDto) {
        val localTask: Task? = task.taskUUID?.let { taskRepository.getTask(it) }
        localTask?.let {
            taskViewModel.deleteTask(it.taskUUID.toString())
            taskAdapter.removeTask(task.taskUUID)
        }
    }

    //todo добавить фабричный метод в EditTaskActivity
    override fun onTaskClick(task: TaskItemDto) {
        showEditTaskDialog(task)
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view_tasks)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        createTaskButton = findViewById(R.id.button_create_task)
    }

    private fun showCreateTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_task, null)
        val editTextTaskName = dialogView.findViewById<EditText>(R.id.editTextTaskName)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val editTextStartDate: EditText = dialogView.findViewById(R.id.editTextStartDate)
        val editTextEndDate: EditText = dialogView.findViewById(R.id.editTextEndDate)
        val editTextDescription: EditText = dialogView.findViewById(R.id.editTextDescription)

        val statuses = arrayOf("Новая", "В процессе", "Завершена")
        val priorities = arrayOf("Низкий", "Средний", "Высокий")

        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        spinnerPriority.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Создать задачу")
            .setView(dialogView)
            .setNegativeButton("Отмена", null)

        dialog.setPositiveButton("Создать", null)

        val alertDialog = dialog.create()

        alertDialog.setOnShowListener {
            val button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val taskName = editTextTaskName.text.toString().trim()
                val taskStatusString = spinnerStatus.selectedItem.toString()
                val taskPriorityString = spinnerPriority.selectedItem.toString()
                val taskDescription = editTextDescription.text.toString()

                if (taskName.isBlank()) {
                    Toast.makeText(this, "Пожалуйста, задайте название задачи", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val taskPriority = TaskPriority.fromString(taskPriorityString)
                val taskStatus = TaskStatus.fromString(taskStatusString)

                val taskDtoRequest = userId?.let { id ->
                    TaskDtoCreateRequest(
                        userId = id,
                        title = taskName,
                        description = taskDescription,
                        priority = taskPriority,
                        taskStatus = taskStatus,
                        dueDate = LocalDate.now().toString()
                    )
                }
                if (taskDtoRequest != null) {
                    taskViewModel.createTask(taskDtoRequest)
                } else {
                    Toast.makeText(this, "Войдите в систему!", Toast.LENGTH_SHORT).show()
                }
                taskAdapter.notifyDataSetChanged()
                alertDialog.dismiss()
            }
        }

        editTextStartDate.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                editTextStartDate.setText(selectedDate)
            }
        }

        editTextEndDate.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                editTextEndDate.setText(selectedDate)
            }
        }

        alertDialog.show()
    }

    private fun showEditTaskDialog(taskItemDto: TaskItemDto) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_task, null)
        val editTextTaskName = dialogView.findViewById<EditText>(R.id.editTextTaskName)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val editTextStartDate: EditText = dialogView.findViewById(R.id.editTextStartDate)
        val editTextEndDate: EditText = dialogView.findViewById(R.id.editTextEndDate)
        val editTextDescription: EditText = dialogView.findViewById(R.id.editTextDescription)

        editTextTaskName.setText(taskItemDto.title)
        editTextDescription.setText(taskItemDto.description)
        editTextStartDate.setText(taskItemDto.startDate)
        editTextEndDate.setText(taskItemDto.dueDate)

        val statuses = arrayOf("Новая", "В процессе", "Завершена")
        val priorities = arrayOf("Низкий", "Средний", "Высокий")

        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        spinnerPriority.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)

        spinnerStatus.setSelection(statuses.indexOf(taskItemDto.status.toString()))
        spinnerPriority.setSelection(priorities.indexOf(taskItemDto.priority.toString()))

        val dialog = AlertDialog.Builder(this)
            .setTitle("Редактировать задачу")
            .setView(dialogView)
            .setNegativeButton("Отмена", null)

        dialog.setPositiveButton("Сохранить", null)

        val alertDialog = dialog.create()

        alertDialog.setOnShowListener {
            val button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val taskName = editTextTaskName.text.toString().trim()
                val taskStatusString = spinnerStatus.selectedItem.toString()
                val taskPriorityString = spinnerPriority.selectedItem.toString()
                val taskDescription = editTextDescription.text.toString()

                if (taskName.isBlank()) {
                    Toast.makeText(this, "Пожалуйста, задайте название задачи", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val taskPriority = TaskPriority.fromString(taskPriorityString)
                val taskStatus = TaskStatus.fromString(taskStatusString)

                val updatedTask = userId?.let { userId ->
                    TaskUpdateDto(
                        taskId = taskItemDto.taskUUID!!,
                        userId = userId,
                        title = taskName,
                        description = taskDescription,
                        priority = taskPriority,
                        status = taskStatus,
                        startDate = editTextStartDate.text.toString(),
                        dueDate = editTextEndDate.text.toString()
                    )
                }
                if (updatedTask != null) {
                    taskViewModel.updateTask(updatedTask)
                }
                taskAdapter.notifyDataSetChanged()
                alertDialog.dismiss()
            }
        }

        editTextStartDate.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                editTextStartDate.setText(selectedDate)
            }
        }

        editTextEndDate.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                editTextEndDate.setText(selectedDate)
            }
        }

        alertDialog.show()
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate =
                    String.format("%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear)
                onDateSelected(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun setupMenu(navigationView: NavigationView) {
        val menu = navigationView.menu
        menu.clear()

        if (isLoggedIn) {
            menu.add(0, R.id.nav_profile, 0, "Профиль")
            menu.add(0, R.id.nav_logout, 1, "Выйти")
        } else {
            menu.add(0, R.id.nav_register, 0, "Зарегистрироваться")
            menu.add(0, R.id.nav_login, 1, "Войти")
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork?.let {
                connectivityManager.getNetworkCapabilities(it)
            }
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}
