package ru.tim.gesturerecognition

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Главное окно приложения.
 */
class Menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        val cameraButton = findViewById<FloatingActionButton>(R.id.cameraButton)
        cameraButton.setOnClickListener {
            val intent = Intent(this, Camera::class.java)
            startActivity(intent)
        }

        val micButton = findViewById<FloatingActionButton>(R.id.micButton)
        micButton.setOnClickListener {
            val intent = Intent(this, Microphone::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<FloatingActionButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
    }
}