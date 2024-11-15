package ru.andreycherenkov.taskmaster.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.andreycherenkov.taskmaster.api.dto.TaskDtoResponse
import ru.andreycherenkov.taskmaster.api.dto.TaskItemDto
import java.time.LocalDate
import java.time.LocalDateTime
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

    fun addTask(taskDtoResponse: TaskDtoResponse) {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_TASK_UUID, taskDtoResponse.taskId.toString())
            put(COLUMN_USER_ID, taskDtoResponse.userId.toString())
            put(COLUMN_TASK_TITLE, taskDtoResponse.title)
            put(COLUMN_TASK_DESCRIPTION, taskDtoResponse.description)
            put(
                COLUMN_TASK_PRIORITY,
                taskDtoResponse.priority.ordinal
            )
            put(
                COLUMN_TASK_STATUS,
                taskDtoResponse.taskStatus.name
            )
            put(COLUMN_TASK_DUE_DATE, taskDtoResponse.dueDate)
            put(COLUMN_TASK_START_DATE, taskDtoResponse.startDate)
            put(
                COLUMN_TASK_UPDATED_AT,
                LocalDate.now().toString()
            )
        }

        db.insert(TABLE_TASKS, null, values)
        db.close()
    }


    @SuppressLint("Range")
    fun getTask(id: Long): Task? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COLUMN_TASK_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        var task: Task? = null
        if (cursor != null && cursor.moveToFirst()) {
            val taskUUID = UUID.fromString(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_UUID)))
            val userId = UUID.fromString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)))
            val title = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE))
            val description = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION))
            val priority = TaskPriority.values()[cursor.getInt(cursor.getColumnIndex(COLUMN_TASK_PRIORITY))]
            val status = TaskStatus.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_STATUS)))
            val startDate = LocalDate.parse(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_START_DATE)))
            val dueDate = LocalDate.parse(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DUE_DATE)))
            val updatedAt = LocalDate.parse(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_UPDATED_AT)))

            task = Task(
                taskUUID = taskUUID,
                userId = userId,
                title = title,
                description = description,
                priority = priority,
                status = status,
                startDate = startDate.toString(),
                dueDate = dueDate.toString(),
                updatedAt = updatedAt.toString()
            )
        }

        cursor?.close()
        return task
    }


    @SuppressLint("Range")
    fun getTask(id: UUID?): Task? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COLUMN_TASK_UUID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val taskUUID = UUID.fromString(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_UUID)))
            val userId = UUID.fromString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)))
            val title = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE))
            val description = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION))
            val priority = TaskPriority.entries[cursor.getInt(cursor.getColumnIndex(COLUMN_TASK_PRIORITY))]
            val status = TaskStatus.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_STATUS)))
            val startDate = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_START_DATE))
            val dueDate = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_DUE_DATE))
            val updatedAt = cursor.getString(cursor.getColumnIndex(COLUMN_TASK_UPDATED_AT))

            Task(
                taskUUID = taskUUID,
                userId = userId,
                title = title,
                description = description,
                priority = priority,
                status = status,
                startDate = startDate?.toString(),
                dueDate = dueDate?.toString(),
                updatedAt = updatedAt.toString()
            ).also {
                cursor.close()
            }
        } else {
            cursor?.close()
            null
        }
    }

    @SuppressLint("Range") // TODO: пофиксить
    fun getAllTasks(): MutableList<TaskItemDto> {
        val tasks = mutableListOf<TaskItemDto>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_TASKS", null)

        if (cursor.moveToFirst()) {
            do {
                val task = TaskItemDto(
                    taskUUID = UUID.fromString(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_UUID))),
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

    fun updateTask(taskDto: TaskDtoResponse) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, taskDto.userId.toString())
            put(COLUMN_TASK_TITLE, taskDto.title)
            put(COLUMN_TASK_DESCRIPTION, taskDto.description)
            put(COLUMN_TASK_PRIORITY, taskDto.priority.ordinal)
            put(COLUMN_TASK_STATUS, taskDto.taskStatus.name)
            put(COLUMN_TASK_START_DATE, taskDto.startDate)
            taskDto.dueDate?.let { put(COLUMN_TASK_DUE_DATE, it) }
            put(COLUMN_TASK_UPDATED_AT, System.currentTimeMillis().toString())
        }

        db.update(TABLE_TASKS, values, "$COLUMN_TASK_UUID=?", arrayOf(taskDto.taskId.toString()))
        db.close()
    }

    // Delete
    fun deleteTask(id: String) {
        val db = writableDatabase
        db.delete(TABLE_TASKS, "$COLUMN_TASK_UUID=?", arrayOf(id))
        db.close()
    }
}

