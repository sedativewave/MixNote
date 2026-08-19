package com.mixnote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mixnote.data.Note
import com.mixnote.views.MixNoteCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var drawingCanvas: MixNoteCanvas
    
    private var isDrawingMode = false
    private var isEraserMode = false
    private var currentNoteId: Long = -1 // -1 значит новая заметка

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        editTitle = findViewById(R.id.editTitle)
        editContent = findViewById(R.id.editContent)
        drawingCanvas = findViewById(R.id.drawingCanvas)
        
        val btnToggleDraw = findViewById<Button>(R.id.btnToggleDraw)
        val btnToggleEraser = findViewById<Button>(R.id.btnToggleEraser)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Проверяем, открыли ли мы существующую заметку
        currentNoteId = intent.getLongExtra("NOTE_ID", -1)
        if (currentNoteId != -1L) {
            loadNoteData()
        }

        btnToggleDraw.setOnClickListener {
            isDrawingMode = !isDrawingMode
            drawingCanvas.isDrawingEnabled = isDrawingMode

            if (isDrawingMode) {
                btnToggleDraw.text = "Писать текст"
                btnToggleEraser.visibility = View.VISIBLE
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editContent.windowToken, 0)
                editContent.clearFocus()
            } else {
                btnToggleDraw.text = "Рисовать"
                btnToggleEraser.visibility = View.GONE
                isEraserMode = false
                drawingCanvas.setEraserMode(false)
            }
        }

        btnToggleEraser.setOnClickListener {
            isEraserMode = !isEraserMode
            drawingCanvas.setEraserMode(isEraserMode)
            btnToggleEraser.text = if (isEraserMode) "Кисть" else "Ластик"
        }

        btnSave.setOnClickListener { saveNote() }
    }

    private fun loadNoteData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = (application as MixNoteApp).database
            val note = db.noteDao().getNoteById(currentNoteId)
            
            if (note != null) {
                withContext(Dispatchers.Main) {
                    editTitle.setText(note.title)
                    editContent.setText(note.content)
                    
                    // Загружаем рисунок, если он был
                    if (note.drawingPath != null) {
                        val bitmap = BitmapFactory.decodeFile(note.drawingPath)
                        if (bitmap != null) {
                            drawingCanvas.loadDrawing(bitmap)
                        }
                    }
                }
            }
        }
    }

    private fun saveNote() {
        val title = editTitle.text.toString().trim()
        val content = editContent.text.toString().trim()

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Заметка пуста", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = drawingCanvas.getBitmap()
            var imagePath: String? = null

            if (bitmap != null) {
                val file = File(filesDir, "drawing_${System.currentTimeMillis()}.png")
                try {
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    imagePath = file.absolutePath
                } catch (e: Exception) { e.printStackTrace() }
            }

            // Если id = 0, Room создаст новую запись. Если id > 0, обновит старую.
            val saveId = if (currentNoteId == -1L) 0 else currentNoteId
            val note = Note(id = saveId, title = title, content = content, drawingPath = imagePath)
            
            val database = (application as MixNoteApp).database
            database.noteDao().insert(note)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(this@NoteEditorActivity, "Сохранено!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
