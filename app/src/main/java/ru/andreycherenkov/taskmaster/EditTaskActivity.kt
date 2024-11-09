package ru.andreycherenkov.taskmaster

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class EditTaskActivity : AppCompatActivity() {

    private lateinit var taskStatusSpinner: Spinner
    private lateinit var taskPrioritySpinner: Spinner
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)
        initViews()

        val statuses = arrayOf("Новая", "В процессе", "Завершена")
        val priorities = arrayOf("Низкий", "Средний", "Высокий")


        taskStatusSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        taskPrioritySpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)

        startDateEditText.setOnClickListener {
            showDatePickerDialog { date ->
                startDateEditText.setText(
                    date
                )
            }
        }
        endDateEditText.setOnClickListener {
            showDatePickerDialog { date ->
                endDateEditText.setText(
                    date
                )
            }
        }

        // Обработка нажатия кнопки "Сохранить изменения"
        saveButton.setOnClickListener {
            saveTask()
        }
    }

    private fun initViews() {
        taskStatusSpinner = findViewById(R.id.taskStatusSpinner)
        taskPrioritySpinner = findViewById(R.id.taskPrioritySpinner)
        startDateEditText = findViewById(R.id.startDateEditText)
        endDateEditText = findViewById(R.id.endDateEditText)
        saveButton = findViewById(R.id.saveButton)
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

    private fun saveTask() {
        // Здесь вы можете добавить логику для сохранения задачи,
        // например, отправка данных на сервер или сохранение в базе данных.
    }
}
