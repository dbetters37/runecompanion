package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Base64
import android.view.View
import java.io.ByteArrayOutputStream

object ScreenVisionHelper {

    /**
     * Captures a low-resolution JPEG Base64 representation of a View or simulated screen frame
     * optimized for Gemini multimodal vision analysis with minimal bandwidth usage.
     */
    fun captureLowResBase64(view: View?, width: Int = 240, height: Int = 480): String {
        return try {
            val bitmap = if (view != null && view.width > 0 && view.height > 0) {
                val orig = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(orig)
                view.draw(canvas)
                Bitmap.createScaledBitmap(orig, width, height, true)
            } else {
                createSyntheticScreenFrame(width, height)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            createFallbackBase64(width, height)
        }
    }

    private fun createSyntheticScreenFrame(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        val textPaint = Paint().apply {
            color = Color.YELLOW
            textSize = 14f
            isAntiAlias = true
        }
        canvas.drawText("OSRS Companion Screen Perception", 10f, 40f, textPaint)
        return bitmap
    }

    private fun createFallbackBase64(width: Int, height: Int): String {
        val bitmap = createSyntheticScreenFrame(width, height)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
