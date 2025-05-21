package com.example.calendar_now

import android.content.res.Configuration
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var selectedDate: String = ""
    private val eventsMap = mutableMapOf<String, MutableList<Event>>()

    data class Event(
        val title: String,
        val description: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val imageButton = findViewById<ImageView>(R.id.imageView)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val selectedDateText = findViewById<TextView>(R.id.textView)

        // Устанавливаем локаль на русский
        val locale = Locale("ru", "RU")
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Обработка системных окон (если нужно)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calendarView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация выбранной даты - текущая дата календаря
        val currentDate = Date(calendarView.date)
        selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(currentDate)

        // Показываем мероприятия на текущую дату
        showEventsForDate(selectedDate, selectedDateText)

        // Обработчик нажатия на кнопку-картинку для добавления события
        imageButton.setOnClickListener {
            showEventEditDialog(selectedDate, selectedDateText)
        }

        // Обработчик выбора даты в календаре
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
            showEventsForDate(selectedDate, selectedDateText)
        }
    }

    // Функция отображения мероприятий на выбранную дату
    private fun showEventsForDate(date: String, textView: TextView) {
        val events = eventsMap[date]
        if (events.isNullOrEmpty()) {
            textView.text = "$date:\nМероприятия отсутствуют"
        } else {
            val builder = StringBuilder()
            builder.append("$date:\n")
            events.forEachIndexed { index, event ->
                builder.append("${index + 1}. ${event.title}\n${event.description}\n\n")
            }
            textView.text = builder.toString()
        }
    }

    // Диалог добавления нового мероприятия
    private fun showEventEditDialog(date: String, textView: TextView) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_event_edit, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.editEventTitle)
        val editDescription = dialogView.findViewById<EditText>(R.id.editEventDescription)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Добавить мероприятие")
            .setView(dialogView)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()

        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSaveEvent)
        buttonSave.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val description = editDescription.text.toString().trim()

            if (title.isEmpty()) {
                editTitle.error = "Введите название"
                return@setOnClickListener
            }

            saveEvent(date, Event(title, description))
            dialog.dismiss()

            // Обновляем список мероприятий после добавления
            showEventsForDate(date, textView)
            Toast.makeText(this, "Мероприятие \"$title\" сохранено", Toast.LENGTH_SHORT).show()
        }
    }

    // Сохраняем мероприятие в мапу
    private fun saveEvent(date: String, event: Event) {
        val list = eventsMap.getOrPut(date) { mutableListOf() }
        list.add(event)
    }
}
