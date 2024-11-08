package ru.andreycherenkov.taskmaster.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoResponse
import ru.andreycherenkov.taskmaster.api.dto.TaskItemDto
import java.time.format.DateTimeFormatter
import java.util.UUID

class TaskRepository(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "task.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_TASKS = "tasks"
        private const val COLUMN_TASK_ID = "id"
        private const val COLUMN_TASK_UUID = "task_uuid"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_TASK_TITLE = "title"
        private const val COLUMN_TASK_DESCRIPTION = "description"
        private const val COLUMN_TASK_PRIORITY = "priority"
        private const val COLUMN_TASK_STATUS = "status"
        private const val COLUMN_TASK_START_DATE = "start_date"
        private const val COLUMN_TASK_DUE_DATE = "due_date"
        private const val COLUMN_TASK_UPDATED_AT = "updated_at"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_TASK_ID LONG PRIMARY KEY,
                $COLUMN_TASK_UUID TEXT,
                $COLUMN_USER_ID INTEGER,
                $COLUMN_TASK_TITLE TEXT NOT NULL,
                $COLUMN_TASK_DESCRIPTION TEXT,
                $COLUMN_TASK_PRIORITY INTEGER,
                $COLUMN_TASK_STATUS TEXT,
                $COLUMN_TASK_START_DATE TEXT,
                $COLUMN_TASK_DUE_DATE TEXT,
                $COLUMN_TASK_UPDATED_AT TEXT
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    fun addTask(taskDtoResponse: TaskDtoResponse): Long {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_USER_ID, taskDtoResponse.userId.toString()) // UUID пользователя
            put(COLUMN_TASK_TITLE, taskDtoResponse.title) // Заголовок задачи
            put(COLUMN_TASK_DESCRIPTION, taskDtoResponse.description) // Описание задачи
            put(
                COLUMN_TASK_PRIORITY,
                taskDtoResponse.priority.ordinal
            ) // Приоритет задачи (предполагается, что это enum)
            put(
                COLUMN_TASK_STATUS,
                taskDtoResponse.taskStatus.name
            ) // Статус задачи (предполагается, что это enum)
            put(COLUMN_TASK_DUE_DATE, taskDtoResponse.dueDate?.toString()) // Срок выполнения задачи
            put(COLUMN_TASK_START_DATE, taskDtoResponse.startDate.toString())
            put(
                COLUMN_TASK_UPDATED_AT,
                System.currentTimeMillis().toString()
            ) // Время обновления задачи
        }

        val id = db.insert(TABLE_TASKS, null, values)

        db.close()
        return id
    }


    // Read
    fun getTask(id: String): Cursor? {
        val db = this.readableDatabase
        return db.query(TABLE_TASKS, null, "$COLUMN_TASK_ID=?", arrayOf(id), null, null, null)
    }

    @SuppressLint("Range") // TODO: пофиксить
    fun getAllTasks(): MutableList<TaskItemDto> {
        val tasks = mutableListOf<TaskItemDto>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_TASKS", null)

        if (cursor.moveToFirst()) {
            do {
                val task = TaskItemDto(
                    taskId = cursor.getLong(cursor.getColumnIndex(COLUMN_TASK_ID)),
                    title = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE)),
                    description = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION)),
                    status = TaskStatus.valueOf(
                        cursor.getString(
                            cursor.getColumnIndex(
                                COLUMN_TASK_STATUS
                            )
                        )
                    ),
                    priority = when (cursor.getInt(cursor.getColumnIndex(COLUMN_TASK_PRIORITY))) {
                        0 -> TaskPriority.LOW
                        1 -> TaskPriority.MEDIUM
                        2 -> TaskPriority.HIGH
                        else -> TaskPriority.UNDEFINED
                    },
                    startDate = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_START_DATE)) ?: "",
                    dueDate = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DUE_DATE)) ?: ""
                )

                tasks.add(task)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return tasks
    }


    // Update
    fun updateTask(
        id: String,
        userId: Int?,
        title: String?,
        description: String?,
        priority: Int?,
        status: String?,
        startDate: String?,
        endDate: String?
    ) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            userId?.let { put(COLUMN_USER_ID, it) }
            title?.let { put(COLUMN_TASK_TITLE, it) }
            description?.let { put(COLUMN_TASK_DESCRIPTION, it) }
            priority?.let { put(COLUMN_TASK_PRIORITY, it) }
            status?.let { put(COLUMN_TASK_STATUS, it) }
            startDate?.let { put(COLUMN_TASK_START_DATE, it) }
            endDate?.let { put(COLUMN_TASK_DUE_DATE, it) }
            put(COLUMN_TASK_UPDATED_AT, System.currentTimeMillis().toString())
        }

        db.update(TABLE_TASKS, values, "$COLUMN_TASK_ID=?", arrayOf(id))
        db.close()
    }

    // Delete
    fun deleteTask(id: String) {
        val db = this.writableDatabase
        db.delete(TABLE_TASKS, "$COLUMN_TASK_ID=?", arrayOf(id))
        db.close()
    }
}

