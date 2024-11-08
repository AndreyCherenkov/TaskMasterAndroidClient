package ru.andreycherenkov.taskmaster

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoCreateRequest
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoResponse
import ru.andreycherenkov.taskmaster.db.TaskRepository
import ru.andreycherenkov.taskmaster.api.dto.TaskItemDto
import ru.andreycherenkov.taskmaster.db.Task
import ru.andreycherenkov.taskmaster.db.TaskPriority
import ru.andreycherenkov.taskmaster.db.TaskStatus
import java.time.LocalDate
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    private val taskRepository = TaskRepository(this)
    private lateinit var taskList: MutableList<TaskItemDto>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var navigationView: NavigationView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var createTaskButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()

        setTaskAdapter()

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {}
                R.id.nav_register -> {}
            }
            drawerLayout.closeDrawers()
            true
        }

        createTaskButton.setOnClickListener {
            showCreateTaskDialog()
        }

    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    private fun setTaskAdapter() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(taskList)
        recyclerView.adapter = taskAdapter
    }

    private fun initViews() {
        taskList = taskRepository.getAllTasks()
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

                val startDate = editTextStartDate.text.toString().trim()
                val endDate = editTextEndDate.text.toString().trim()

                // Валидация полей
                if (taskName.isBlank()) {
                    Toast.makeText(this, "Пожалуйста, задайте название задачи", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener // Не закрываем диалог
                }

                val taskPriority = TaskPriority.fromString(taskPriorityString)
                val taskStatus = TaskStatus.fromString(taskStatusString)

                val newTask = TaskItemDto(
                    taskId = null,
                    title = taskName,
                    description = taskDescription,
                    status = taskStatus,
                    priority = taskPriority,
                    startDate = startDate,
                    dueDate = endDate
                )

                val test = TaskDtoCreateRequest(
                    userId = UUID.fromString("59564db6-6e3f-49d3-94b0-b3317f12b13e"),
                    title = taskName,
                    description = taskDescription,
                    priority = taskPriority,
                    taskStatus = taskStatus,
                    dueDate = LocalDate.now().toString()
                )

                Log.d("TaskStatus", "Selected status: '$taskPriority'")

                addTask(test)
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


    private fun addTaskToList(task: TaskItemDto) {
        taskList.add(task)
        taskAdapter.notifyDataSetChanged()
    }

    private fun addTask(taskDtoCreateRequest: TaskDtoCreateRequest) {
        if (isNetworkAvailable(this)) {
            val taskApi = RetrofitClient.taskApi
            taskApi.createTask(taskDtoCreateRequest).enqueue(object : Callback<TaskDtoResponse> {
                override fun onResponse(
                    call: Call<TaskDtoResponse>,
                    response: Response<TaskDtoResponse>
                ) {
                    if (response.isSuccessful) {
                        val createdTask = response.body()
                        if (createdTask != null) {
                            val id = taskRepository.addTask(createdTask)
                            val taskItem = with(createdTask) {
                                TaskItemDto(
                                    taskId = id,
                                    title = title,
                                    description = description,
                                    status = taskStatus,
                                    priority = priority,
                                    startDate = startDate,
                                    dueDate = dueDate.toString()
                                )
                            }
                            addTaskToList(taskItem)
                        }
                        Logger.getGlobal().log(Level.INFO, "RESPONSE SUCCESSFUL")
                    }
                }

                override fun onFailure(call: Call<TaskDtoResponse>, t: Throwable) {
                    Logger.getGlobal()
                        .log(Level.INFO, "RESPONSE FAILED: ${t.message}")
                }
            })

        } else {
            println("No internet connection available.")
        }

    }

    fun isNetworkAvailable(context: Context): Boolean {
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
}