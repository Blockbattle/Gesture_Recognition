package ru.tim.gesturerecognition

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.tim.gesturerecognition.WordGenerator.Companion.text

/**
 * Активность для редактирования текста.
 * Создаёт экран редактирования текста. Обрабатывает кнопки сохранения и отмены изменений.
 */
class TextEdit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_text)

        val editText = findViewById<EditText>(R.id.editText)
        editText.setText(text)

        val closeButton = findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener {
            finish()
        }

        val saveButton = findViewById<ImageButton>(R.id.saveButton)
        saveButton.setOnClickListener {
            text = editText.text.toString()
            finish()
        }
    }

}