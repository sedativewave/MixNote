package com.mixnote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mixnote.utils.PreferencesManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        // Применяем цвет темы при каждом возвращении на главный экран
        val color = PreferencesManager(this).themeColor
        findViewById<android.widget.LinearLayout>(R.id.topBar).setBackgroundColor(color)
        findViewById<FloatingActionButton>(R.id.fabAddNote).backgroundTintList = android.content.res.ColorStateList.valueOf(color)
    }
    
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNotes)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddNote)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        // Применяем сохраненный цвет темы к кнопке настроек
        val prefs = PreferencesManager(this)
        btnSettings.setBackgroundColor(prefs.themeColor)

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        adapter = NoteAdapter(emptyList()) { clickedNote ->
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("NOTE_ID", clickedNote.id)
            startActivity(intent)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener {
            startActivity(Intent(this, NoteEditorActivity::class.java))
        }

        lifecycleScope.launch {
            val db = (application as MixNoteApp).database
            db.noteDao().getAllNotes().collect { list ->
                adapter.update(list)
            }
        }
    }
}
