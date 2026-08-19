package com.mixnote

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNotes)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddNote)

        // Передаем функцию, которая откроет редактор при клике на карточку
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
