package com.mixnote

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.mixnote.data.Note
import com.mixnote.views.MixNoteCanvas
import com.mixnote.views.ZoomableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var drawingCanvas: MixNoteCanvas
    private lateinit var imageAttachment: ZoomableImageView
    private lateinit var noteContainer: View
    private lateinit var drawingToolbar: LinearLayout

    private var isDrawingMode = false
    private var isEraserMode = false
    private var currentNoteId: Long = -1
    private var selectedImageUri: String? = null
    private var currentTextSize = 18f
    private var currentTextColor = Color.BLACK

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it.toString()
            imageAttachment.setImageURI(uri)
            imageAttachment.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        editTitle = findViewById(R.id.editTitle)
        editContent = findViewById(R.id.editContent)
        drawingCanvas = findViewById(R.id.drawingCanvas)
        imageAttachment = findViewById(R.id.imageAttachment)
        noteContainer = findViewById(R.id.noteContainer)
        drawingToolbar = findViewById(R.id.drawingToolbar)

        val btnToggleDraw = findViewById<Button>(R.id.btnToggleDraw)
        val btnToggleEraser = findViewById<Button>(R.id.btnToggleEraser)
        val btnBrushColor = findViewById<Button>(R.id.btnBrushColor)
        val btnBrushSize = findViewById<Button>(R.id.btnBrushSize)
        val btnTextColor = findViewById<Button>(R.id.btnTextColor)
        val btnTextSize = findViewById<Button>(R.id.btnTextSize)
        val btnAddImage = findViewById<Button>(R.id.btnAddImage)
        val btnShare = findViewById<Button>(R.id.btnShare)
        val btnSave = findViewById<Button>(R.id.btnSave)

        currentNoteId = intent.getLongExtra("NOTE_ID", -1)
        if (currentNoteId != -1L) loadNoteData()

        btnToggleDraw.setOnClickListener {
            isDrawingMode = !isDrawingMode
            drawingCanvas.isDrawingEnabled = isDrawingMode
            if (isDrawingMode) {
                btnToggleDraw.text = "Текст"
                btnToggleEraser.visibility = View.VISIBLE
                drawingToolbar.visibility = View.VISIBLE
                hideKeyboard()
            } else {
                btnToggleDraw.text = "Рисовать"
                btnToggleEraser.visibility = View.GONE
                drawingToolbar.visibility = View.GONE
                isEraserMode = false
                drawingCanvas.setEraserMode(false)
            }
        }

        btnToggleEraser.setOnClickListener {
            isEraserMode = !isEraserMode
            drawingCanvas.setEraserMode(isEraserMode)
            btnToggleEraser.text = if (isEraserMode) "Кисть" else "Ластик"
        }

        // Выбор цвета кисти для рисования
        btnBrushColor.setOnClickListener {
            val colors = arrayOf("Черный", "Красный", "Синий", "Зеленый", "Фиолетовый")
            val colorValues = intArrayOf(Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.parseColor("#FF6200EE"))
            AlertDialog.Builder(this)
                .setTitle("Цвет кисти")
                .setItems(colors) { _, which ->
                    drawingCanvas.setBrushColor(colorValues[which])
                }.show()
        }

        // Выбор толщины линии кисти
        btnBrushSize.setOnClickListener {
            val sizes = arrayOf("Тонкая (5px)", "Средняя (10px)", "Толстая (20px)", "Жирная (35px)")
            val sizeValues = floatArrayOf(5f, 10f, 20f, 35f)
            AlertDialog.Builder(this)
                .setTitle("Толщина линии")
                .setItems(sizes) { _, which ->
                    drawingCanvas.setBrushSize(sizeValues[which])
                }.show()
        }

        btnTextColor.setOnClickListener {
            val colors = arrayOf("Черный", "Красный", "Синий", "Зеленый")
            val colorValues = intArrayOf(Color.BLACK, Color.RED, Color.BLUE, Color.GREEN)
            AlertDialog.Builder(this)
                .setTitle("Цвет текста")
                .setItems(colors) { _, which ->
                    currentTextColor = colorValues[which]
                    editTitle.setTextColor(currentTextColor)
                    editContent.setTextColor(currentTextColor)
                }.show()
        }

        btnTextSize.setOnClickListener {
            val sizes = arrayOf("Маленький", "Средний", "Большой", "Огромный")
            val sizeValues = floatArrayOf(14f, 18f, 24f, 32f)
            AlertDialog.Builder(this)
                .setTitle("Размер текста")
                .setItems(sizes) { _, which ->
                    currentTextSize = sizeValues[which]
                    editContent.textSize = currentTextSize
                    editTitle.textSize = currentTextSize + 6
                }.show()
        }

        btnAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnShare.setOnClickListener {
            shareNoteAsJpeg()
        }

        btnSave.setOnClickListener { saveNote() }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editContent.windowToken, 0)
        editContent.clearFocus()
    }

    private fun loadNoteData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = (application as MixNoteApp).database
            val note = db.noteDao().getNoteById(currentNoteId)
            note?.let {
                withContext(Dispatchers.Main) {
                    editTitle.setText(it.title)
                    editContent.setText(it.content)
                    currentTextSize = it.textSize
                    editContent.textSize = currentTextSize
                    editTitle.textSize = currentTextSize + 6

                    if (it.drawingPath != null) {
                        val bmp = BitmapFactory.decodeFile(it.drawingPath)
                        bmp?.let { drawingCanvas.loadDrawing(it) }
                    }
                    if (it.imagePath != null) {
                        selectedImageUri = it.imagePath
                        imageAttachment.setImageURI(Uri.parse(it.imagePath))
                        imageAttachment.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun shareNoteAsJpeg() {
        hideKeyboard()
        try {
            noteContainer.isDrawingCacheEnabled = true
            noteContainer.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(noteContainer.drawingCache)
            noteContainer.isDrawingCacheEnabled = false

            val imageFile = File(cacheDir, "shared_note.jpg")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Отправить заметку:"))
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote() {
        val title = editTitle.text.toString().trim()
        val content = editContent.text.toString().trim()

        if (title.isEmpty() && content.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Заметка пуста", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = drawingCanvas.getBitmap()
            var drawingPath: String? = null

            if (bitmap != null) {
                val file = File(filesDir, "drawing_${System.currentTimeMillis()}.png")
                try {
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    drawingPath = file.absolutePath
                } catch (e: Exception) { e.printStackTrace() }
            }

            val saveId = if (currentNoteId == -1L) 0 else currentNoteId
            val note = Note(
                id = saveId,
                title = title,
                content = content,
                drawingPath = drawingPath,
                imagePath = selectedImageUri,
                textSize = currentTextSize
            )

            val database = (application as MixNoteApp).database
            database.noteDao().insert(note)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@NoteEditorActivity, "Сохранено!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
