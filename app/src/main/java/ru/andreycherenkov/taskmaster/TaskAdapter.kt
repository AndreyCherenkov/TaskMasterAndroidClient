package ru.andreycherenkov.taskmaster

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ru.andreycherenkov.taskmaster.api.dto.TaskItemDto
import ru.andreycherenkov.taskmaster.db.TaskPriority
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskAdapter(private val tasks: List<TaskItemDto>,
                  private val onTaskClick: OnTaskClickListener,
                  private val onTaskLongClick: OnTaskClickListener) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val textViewTaskName: TextView = itemView.findViewById(R.id.textViewTaskName)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        val textViewDates: TextView = itemView.findViewById(R.id.textViewDates)



        init {

            itemView.setOnClickListener {
                onTaskClick.onTaskClick(tasks[adapterPosition])
            }

            itemView.setOnLongClickListener {
                onTaskLongClick.onTaskLongClick(tasks[adapterPosition])
                true
            }
        }
    }

    interface OnTaskClickListener {
        fun onTaskLongClick(task: TaskItemDto)
        fun onTaskClick(task: TaskItemDto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.textViewTaskName.text = task.title
        holder.textViewDescription.text = task.description
        holder.textViewStatus.text = "Статус задачи: ${task.status.name.lowercase()}"

        val startDate = task.startDate.ifBlank {
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }

        //todo исправить костыль с == "null". Костыль появился из-за проблем с сериализацией дат
        holder.textViewDates.text = if (task.dueDate == "null" || task.dueDate.isBlank()) {
            "Дата создания: $startDate"
        } else {
            "${task.startDate} - ${task.dueDate}"
        }

        when (task.priority) {
            TaskPriority.HIGH -> holder.cardView.setCardBackgroundColor(Color.RED)
            TaskPriority.MEDIUM -> holder.cardView.setCardBackgroundColor(Color.YELLOW)
            TaskPriority.LOW -> holder.cardView.setCardBackgroundColor(Color.GREEN)
            TaskPriority.UNDEFINED -> holder.cardView.setCardBackgroundColor(Color.YELLOW)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}


