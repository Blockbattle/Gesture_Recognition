package ru.tim.gesturerecognition

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.tim.gesturerecognition.WordGenerator.Companion.text
import ru.tim.gesturerecognition.WordGenerator.Companion.wordProcessing
import java.util.*

/**
 * Активность для распознавания речи и отображения её на экране.
 */
class Microphone : AppCompatActivity() {
    /**
     * Создаёт активность.
     * Обрабатывает нажатия кнопок.
     * @param savedInstanceState сохранённое состояние активности.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.microphone)

        val linearLayout = findViewById<LinearLayout>(R.id.layout)
        linearLayout.setOnClickListener {
            val intent = Intent(this, TextEdit::class.java)
            startActivity(intent)
        }

        val switchButton = findViewById<FloatingActionButton>(R.id.switchButton)
        switchButton.setOnClickListener {
            Camera.mode = Camera.Mode.NONE_M

            GlobalScope.launch() {
                text = wordProcessing()

                Handler(Looper.getMainLooper()).post {
                    val textView: TextView = findViewById(R.id.textView)
                    textView.text = ""
                    TextDialog.showDialog(this@Microphone)
                    text = ""
                }
            }
        }

        val checkButton = findViewById<FloatingActionButton>(R.id.checkButton)
        checkButton.setOnClickListener {
            GlobalScope.launch() {
                text = wordProcessing()

                Handler(Looper.getMainLooper()).post {
                    val textView: TextView = findViewById(R.id.textView)
                    textView.text = text
                }
            }
        }

        val micButton = findViewById<FloatingActionButton>(R.id.micButton)
        micButton.setOnClickListener {
            promptSpeechInput()
        }

        promptSpeechInput()
    }

    override fun onResume() {
        super.onResume()

        val textView = findViewById<TextView>(R.id.textView)
        textView.text = text
    }

    override fun onBackPressed() {
        super.onBackPressed()
        text = ""
    }

    /**
     * Создаёт и запускает окно распознавания речи.
     * Задаёт параметры: язык, подсказка и др.
     */
    private fun promptSpeechInput() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru")
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Пожалуйста, говорите")

            startActivityForResult(intent, 1001)
        } catch(e: ActivityNotFoundException)
        {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.google_speech_recognition))
                )
            startActivity(browserIntent)
        }
    }


    /**
     * Добавляет результат распознавания речи в текстовое поле.
     * Запускается при возвращении результата от окна распознавания речи.
     * @param requestCode код запроса
     * @param resultCode код результата
     * @param data данные с результатом распознавания речи
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Определяем результат, возвращаемый действием распознавания голоса
        if (requestCode == 1001) {
            // Определяем статус возвращаемого результата - успех
            if (resultCode == RESULT_OK) {
                // Получить результат распознавания голоса
                val matches: ArrayList<String>? =
                    data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (matches != null) {
                    text = text + " " + matches[0]
                    val textView = findViewById<TextView>(R.id.textView)
                    textView.text = text
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}