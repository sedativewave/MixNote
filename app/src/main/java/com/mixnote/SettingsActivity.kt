package com.mixnote

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mixnote.utils.PreferencesManager

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = PreferencesManager(this)

        findViewById<Button>(R.id.btnColorPurple).setOnClickListener {
            prefs.themeColor = Color.parseColor("#7E57C2")
            finish()
        }
        findViewById<Button>(R.id.btnColorBlue).setOnClickListener {
            prefs.themeColor = Color.parseColor("#1E88E5")
            finish()
        }
        findViewById<Button>(R.id.btnColorGreen).setOnClickListener {
            prefs.themeColor = Color.parseColor("#43A047")
            finish()
        }
    }
}
