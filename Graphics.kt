package com.example.myapplication.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import com.example.myapplication.model.Farm
import org.osmdroid.util.GeoPoint
import java.util.Locale
import kotlin.math.max

object MapGraphics {

    fun loadAppLogoBitmap(context: Context): Bitmap {
        val customDrawableId = context.resources.getIdentifier("app_logo", "drawable", context.packageName)
        if (customDrawableId != 0) {
            val decoded = BitmapFactory.decodeResource(context.resources, customDrawableId)
            if (decoded != null) return decoded
        }
        return createMapLogoBitmap(context)
    }

    fun getCropAccentColor(crop: String): Int {
        val c = crop.lowercase(Locale.US)
        return when {
            c.contains("sugarcane") -> Color.parseColor("#137333")
            c.contains("rice") -> Color.parseColor("#1A73E8")
            c.contains("coconut") -> Color.parseColor("#007B83")
            c.contains("pineapple") || c.contains("mango") -> Color.parseColor("#E37400")
            c.contains("coffee") || c.contains("cacao") -> Color.parseColor("#C5221F")
            else -> Color.parseColor("#7627BB")
        }
    }

    fun createMapLogoBitmap(context: Context): Bitmap {
        val density = context.resources.displayMetrics.density
        val size = (200 * density).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cx = size / 2f
        val cy = size / 2f
        val outerRadius = (size / 2f) - (4 * density)

        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, outerRadius, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#137333")
        paint.strokeWidth = 3.5f * density
        canvas.drawCircle(cx, cy, outerRadius - (2 * density), paint)

        paint.style = Paint.Style.FILL
        val mapRadius = outerRadius - (8 * density)
        paint.color = Color.parseColor("#E8F0FE")
        canvas.drawCircle(cx, cy, mapRadius, paint)

        paint.color = Color.parseColor("#81C995")
        val mountainPath = Path().apply {
            moveTo(cx - mapRadius * 0.85f, cy + mapRadius * 0.15f)
            lineTo(cx - mapRadius * 0.35f, cy - mapRadius * 0.32f)
            lineTo(cx + mapRadius * 0.05f, cy + mapRadius * 0.02f)
            lineTo(cx + mapRadius * 0.45f, cy - mapRadius * 0.25f)
            lineTo(cx + mapRadius * 0.85f, cy + mapRadius * 0.15f)
            close()
        }
        canvas.drawPath(mountainPath, paint)

        paint.color = Color.parseColor("#34A853")
        val fieldRect = RectF(cx - mapRadius * 0.82f, cy - mapRadius * 0.05f, cx + mapRadius * 0.82f, cy + mapRadius * 0.82f)
        canvas.drawArc(fieldRect, 0f, 180f, true, paint)

        paint.color = Color.parseColor("#A8DAB5")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.4f * density
        canvas.drawLine(cx - mapRadius * 0.55f, cy + mapRadius * 0.35f, cx + mapRadius * 0.55f, cy + mapRadius * 0.35f, paint)
        canvas.drawLine(cx - mapRadius * 0.42f, cy + mapRadius * 0.52f, cx + mapRadius * 0.42f, cy + mapRadius * 0.52f, paint)

        paint.style = Paint.Style.FILL
        val pinCy = cy - (12 * density)
        val pinRadius = 24 * density
        paint.color = Color.parseColor("#EA4335")
        val pinTail = Path().apply {
            moveTo(cx - pinRadius * 0.78f, pinCy + pinRadius * 0.4f)
            lineTo(cx + pinRadius * 0.78f, pinCy + pinRadius * 0.4f)
            lineTo(cx, cy + (28 * density))
            close()
        }
        canvas.drawPath(pinTail, paint)
        canvas.drawCircle(cx, pinCy, pinRadius, paint)

        paint.color = Color.WHITE
        canvas.drawCircle(cx, pinCy, pinRadius * 0.42f, paint)

        paint.color = Color.parseColor("#0D652D")
        val leafPath = Path().apply {
            moveTo(cx + pinRadius * 0.5f, pinCy - pinRadius * 0.6f)
            quadTo(cx + pinRadius * 1.4f, pinCy - pinRadius * 1.25f, cx + pinRadius * 1.3f, pinCy - pinRadius * 0.25f)
            quadTo(cx + pinRadius * 0.75f, pinCy - pinRadius * 0.1f, cx + pinRadius * 0.5f, pinCy - pinRadius * 0.6f)
            close()
        }
        canvas.drawPath(leafPath, paint)

        return bitmap
    }

    fun createBarangayPinDrawable(context: Context, pinColor: Int): BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val width = (36 * density).toInt()
        val height = (46 * density).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cx = width / 2f
        val cy = 16 * density
        val radius = 14 * density

        paint.color = Color.argb(65, 0, 0, 0)
        canvas.drawCircle(cx, height - 3 * density, 6 * density, paint)

        paint.color = Color.WHITE
        val whiteTip = Path().apply {
            moveTo(cx - radius * 0.85f, cy + radius * 0.35f)
            lineTo(cx + radius * 0.85f, cy + radius * 0.35f)
            lineTo(cx, height - 3 * density)
            close()
        }
        canvas.drawPath(whiteTip, paint)
        canvas.drawCircle(cx, cy, radius, paint)

        val innerR = radius - 2.4f * density
        paint.color = pinColor
        val innerTip = Path().apply {
            moveTo(cx - innerR * 0.82f, cy + innerR * 0.32f)
            lineTo(cx + innerR * 0.82f, cy + innerR * 0.32f)
            lineTo(cx, height - 6 * density)
            close()
        }
        canvas.drawPath(innerTip, paint)
        canvas.drawCircle(cx, cy, innerR, paint)

        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, 4.5f * density, paint)

        return BitmapDrawable(context.resources, bitmap)
    }

    fun createCustomCoordinateBadgeDrawable(context: Context, point: GeoPoint, accentColor: Int): BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val coordText = String.format(Locale.US, "%.5f° N, %.5f° E", point.latitude, point.longitude)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bounds = Rect()
        textPaint.getTextBounds(coordText, 0, coordText.length, bounds)

        val padH = (12 * density).toInt()
        val padV = (7 * density).toInt()
        val pointerH = (10 * density).toInt()
        val boxWidth = bounds.width() + padH * 2
        val boxHeight = bounds.height() + padV * 2
        val totalHeight = boxHeight + pointerH

        val bitmap = Bitmap.createBitmap(boxWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(1f, 1f, boxWidth - 1f, boxHeight - 1f)
        val radius = 10 * density

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, radius, radius, bgPaint)

        val pointerPath = Path().apply {
            moveTo((boxWidth / 2f) - (6 * density), boxHeight - 2f)
            lineTo((boxWidth / 2f) + (6 * density), boxHeight - 2f)
            lineTo(boxWidth / 2f, totalHeight - 2f)
            close()
        }
        canvas.drawPath(pointerPath, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 1.8f * density
        }
        canvas.drawRoundRect(rect, radius, radius, borderPaint)

        canvas.drawText(coordText, padH.toFloat(), (padV + bounds.height()).toFloat(), textPaint)
        return BitmapDrawable(context.resources, bitmap)
    }

    fun createFloatingInfoCardDrawable(
        context: Context,
        farm: Farm,
        accentColor: Int,
        isSelected: Boolean
    ): BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val line1 = "Brgy. ${farm.name} • ${farm.shortAreaLabel()}"
        val line2 = "${farm.crop} (${farm.formattedLat()}°, ${farm.formattedLng()}°)"

        val paintLine1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSelected) Color.WHITE else Color.parseColor("#202124")
            textSize = 10.5f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val paintLine2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSelected) Color.parseColor("#E8F5E9") else accentColor
            textSize = 9.0f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bounds1 = Rect()
        val bounds2 = Rect()
        paintLine1.getTextBounds(line1, 0, line1.length, bounds1)
        paintLine2.getTextBounds(line2, 0, line2.length, bounds2)

        val padH = (11 * density).toInt()
        val padV = (7 * density).toInt()
        val contentWidth = max(bounds1.width(), bounds2.width())
        val width = contentWidth + padH * 2
        val height = bounds1.height() + bounds2.height() + padV * 3

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(1f, 1f, width - 1f, height - 1f)
        val radius = 10 * density

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSelected) accentColor else Color.argb(245, 255, 255, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, radius, radius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = (if (isSelected) 2.5f else 1.8f) * density
        }
        canvas.drawRoundRect(rect, radius, radius, borderPaint)

        canvas.drawText(line1, padH.toFloat(), (padV + bounds1.height()).toFloat(), paintLine1)
        canvas.drawText(line2, padH.toFloat(), (padV * 2 + bounds1.height() + bounds2.height()).toFloat(), paintLine2)

        return BitmapDrawable(context.resources, bitmap)
    }
}