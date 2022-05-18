package ru.tim.gesturerecognition

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Settings : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switch1: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switch2: Switch

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        switch1 = findViewById<Switch>(R.id.switch1)
        switch2 = findViewById<Switch>(R.id.switch2)

        switch1.setOnCheckedChangeListener { compoundButton, isChecked ->
            isCheckText = isChecked
        }
        switch2.setOnCheckedChangeListener { compoundButton, isChecked ->
            isSpace = isChecked
        }
    }

    override fun onPause() {
        super.onPause()
        val editor = prefs.edit()
        editor.putBoolean("check_text", isCheckText).putBoolean("space", isSpace).apply()
    }

    override fun onResume() {
        super.onResume()

        if(prefs.contains("check_text")) {
            isCheckText = prefs.getBoolean("check_text", true)
            switch1.isChecked = isCheckText
        }
        if(prefs.contains("space")) {
            isSpace = prefs.getBoolean("space", false)
            switch2.isChecked = isSpace
        }
    }

    companion object {
        var isCheckText = true
        var isSpace = false
    }
}