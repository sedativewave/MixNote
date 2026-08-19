package com.mixnote.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class MixNoteCanvas @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var drawPath = Path()
    private var drawPaint = Paint()
    private var canvasPaint = Paint(Paint.DITHER_FLAG)
    private var drawCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null

    var isDrawingEnabled = false
    private var isEraserMode = false

    init {
        drawPaint.color = Color.BLACK
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = 10f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            if (canvasBitmap == null) {
                canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            } else {
                canvasBitmap = Bitmap.createScaledBitmap(canvasBitmap!!, w, h, true)
            }
            drawCanvas = Canvas(canvasBitmap!!)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvasBitmap?.let { canvas.drawBitmap(it, 0f, 0f, canvasPaint) }
        if (!isEraserMode && isDrawingEnabled) canvas.drawPath(drawPath, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) return false
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> drawPath.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(x, y)
                if (isEraserMode) {
                    drawCanvas?.drawPath(drawPath, drawPaint)
                    drawPath.reset()
                    drawPath.moveTo(x, y)
                }
            }
            MotionEvent.ACTION_UP -> {
                drawPath.lineTo(x, y)
                drawCanvas?.drawPath(drawPath, drawPaint)
                drawPath.reset()
            }
            else -> return false
        }
        invalidate()
        return true
    }

    fun setEraserMode(isEraser: Boolean) {
        isEraserMode = isEraser
        if (isEraser) {
            drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            drawPaint.strokeWidth = 50f
        } else {
            drawPaint.xfermode = null
            drawPaint.strokeWidth = 10f
        }
    }

    fun setBrushColor(color: Int) {
        drawPaint.color = color
    }

    fun setBrushSize(size: Float) {
        drawPaint.strokeWidth = size
    }

    fun getBitmap(): Bitmap? = canvasBitmap
    
    fun loadDrawing(bitmap: Bitmap) {
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        invalidate()
    }
}
