package com.example.objectsrecognizer.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.objectsrecognizer.data.ImageObject
import com.example.objectsrecognizer.data.ObjectBounds

internal class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var imageObjects: List<ImageObject> = emptyList()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var scaleFactorX: Float = 1f
    private var scaleFactorY: Float = 1f

    private var bounds = Rect()

    private var left: Int = 0
    private var top: Int = 0

    private var imageViewportWidth: Float = 0F
    private var imageViewportHeight: Float = 0F

    init {
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = Color.LTGRAY
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        imageObjects.forEach { imageObject ->
            drawImageObjectOnCanvas(imageObject, canvas)
        }
    }

    /* Draw Necessity Functions */
    private fun drawImageObjectOnCanvas(imageObject: ImageObject, canvas: Canvas) {
        // Object bounds relatively to image
        val objectBoundsOnView = ObjectBounds(
            this.left + imageObject.bounds.left * scaleFactorX,
            this.top + imageObject.bounds.top * scaleFactorY,
            this.left + imageObject.bounds.right * scaleFactorX,
            this.top + imageObject.bounds.bottom * scaleFactorY
        )

        drawImageObjectBox(objectBoundsOnView, canvas)
        drawImageCategoryTextOnCanvas(imageObject.category, objectBoundsOnView, canvas)
    }

    private fun drawImageObjectBox(objectBounds: ObjectBounds, canvas: Canvas) {
        with(objectBounds) {
            val drawableRect = RectF(left, top, right, bottom)
            canvas.drawRect(drawableRect, boxPaint)
        }
    }

    private fun drawImageCategoryTextOnCanvas(
        categoryText: String,
        objectBounds: ObjectBounds,
        canvas: Canvas
    ) {
        textBackgroundPaint.getTextBounds(categoryText, 0, categoryText.length, bounds)

        val textWidth = bounds.width()
        val textHeight = bounds.height()

        with(objectBounds) {
            canvas.drawRect(
                left,
                top,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )

            canvas.drawText(categoryText, left, top + bounds.height(), textPaint)
        }
    }

    /* Objects Setting Functions */
    fun setImageObjectsAndInvalidate(
        imageObjectList: List<ImageObject>,
        imageHeight: Int,
        imageWidth: Int
    ) {
        imageObjects = imageObjectList

        updateDimensions(imageHeight, imageWidth)

        invalidate()
    }

    private fun updateDimensions(
        imageHeight: Int,
        imageWidth: Int
    ) {
        val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
        val overlayAspectRatio = width.toFloat() / height.toFloat()

        if (imageAspectRatio > overlayAspectRatio) {
            imageViewportWidth = width.toFloat()
            imageViewportHeight = imageViewportWidth / imageAspectRatio
        } else {
            imageViewportHeight = height.toFloat()
            imageViewportWidth = imageViewportHeight * imageAspectRatio
        }

        left = ((width.toFloat() - imageViewportWidth) / 2).toInt()
        top = ((height.toFloat() - imageViewportHeight) / 2).toInt()

        scaleFactorX = imageViewportWidth / imageWidth
        scaleFactorY = imageViewportHeight / imageHeight
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}