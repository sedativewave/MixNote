package com.mixnote

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.mixnote.utils.PreferencesManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var colorPreview: View
    private lateinit var seekRed: SeekBar
    private lateinit var seekGreen: SeekBar
    private lateinit var seekBlue: SeekBar
    private var currentColor = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        colorPreview = findViewById(R.id.colorPreview)
        seekRed = findViewById(R.id.seekRed)
        seekGreen = findViewById(R.id.seekGreen)
        seekBlue = findViewById(R.id.seekBlue)
        val btnApply = findViewById<Button>(R.id.btnApplyColor)

        val prefs = PreferencesManager(this)
        currentColor = prefs.themeColor

        // Устанавливаем ползунки на текущий цвет
        seekRed.progress = Color.red(currentColor)
        seekGreen.progress = Color.green(currentColor)
        seekBlue.progress = Color.blue(currentColor)
        updatePreview()

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { updatePreview() }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        seekRed.setOnSeekBarChangeListener(listener)
        seekGreen.setOnSeekBarChangeListener(listener)
        seekBlue.setOnSeekBarChangeListener(listener)

        btnApply.setOnClickListener {
            prefs.themeColor = currentColor
            finish()
        }
    }

    private fun updatePreview() {
        currentColor = Color.rgb(seekRed.progress, seekGreen.progress, seekBlue.progress)
        colorPreview.setBackgroundColor(currentColor)
        findViewById<Button>(R.id.btnApplyColor).setBackgroundColor(currentColor)
    }
}
