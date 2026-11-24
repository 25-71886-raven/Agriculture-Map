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
            c.contains("pineapple") || c.contains("mango") || c.contains("dragon fruit") -> Color.parseColor("#E37400")
            c.contains("coffee") || c.contains("cacao") -> Color.parseColor("#C5221F")
            else -> Color.parseColor("#7627BB")
        }
    }

    fun createMapLogoBitmap(context: Context): Bitmap {
        val density = context.resources.displayMetrics.density
        val size = (200 * density).toInt().coerceAtLeast(1)
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
        val width = (36 * density).toInt().coerceAtLeast(1)
        val height = (46 * density).toInt().coerceAtLeast(1)
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
        val boxWidth = (bounds.width() + padH * 2).coerceAtLeast(1)
        val boxHeight = (bounds.height() + padV * 2).coerceAtLeast(1)
        val totalHeight = (boxHeight + pointerH).coerceAtLeast(1)

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
        val width = (contentWidth + padH * 2).coerceAtLeast(1)
        val height = (bounds1.height() + bounds2.height() + padV * 3).coerceAtLeast(1)

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

    fun parseBarangayCropItems(cropText: String): List<String> {
        val cleaned = cropText.replace(Regex("\\([^)]*\\)"), "")
        return cleaned.split(",", "&")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase(Locale.US) }
    }

    fun createSingleCropCardBitmap(context: Context, cropItemName: String): Bitmap {
        val density = context.resources.displayMetrics.density
        val width = (104 * density).toInt().coerceAtLeast(1)
        val height = (84 * density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val c = cropItemName.lowercase(Locale.US)
        val (bgColor, borderColor) = when {
            c.contains("sugarcane") -> Pair(Color.parseColor("#E6F4EA"), Color.parseColor("#137333"))
            c.contains("rice") -> Pair(Color.parseColor("#E8F0FE"), Color.parseColor("#1A73E8"))
            c.contains("corn") -> Pair(Color.parseColor("#FEF7E0"), Color.parseColor("#F9AB00"))
            c.contains("coconut") || c.contains("nipa") -> Pair(Color.parseColor("#E0F2F1"), Color.parseColor("#007B83"))
            c.contains("banana") -> Pair(Color.parseColor("#FFFDE7"), Color.parseColor("#FBC02D"))
            c.contains("dragon fruit") -> Pair(Color.parseColor("#FCE4EC"), Color.parseColor("#D81B60"))
            c.contains("mango") || c.contains("pineapple") || c.contains("cashew") || c.contains("fruit") ->
                Pair(Color.parseColor("#FFF3E0"), Color.parseColor("#E37400"))
            c.contains("coffee") || c.contains("cacao") -> Pair(Color.parseColor("#FCE8E6"), Color.parseColor("#C5221F"))
            c.contains("fishery") -> Pair(Color.parseColor("#E1F5FE"), Color.parseColor("#0288D1"))
            else -> Pair(Color.parseColor("#F3E8FD"), Color.parseColor("#7627BB"))
        }

        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 12 * density, 12 * density, paint.apply {
            color = bgColor
            style = Paint.Style.FILL
        })
        canvas.drawRoundRect(RectF(1f, 1f, width - 1f, height - 1f), 12 * density, 12 * density, paint.apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 1.6f * density
        })

        paint.style = Paint.Style.FILL
        val cx = width / 2f
        val cy = 32 * density
        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, 22 * density, paint)

        when {
            c.contains("sugarcane") -> {
                paint.color = Color.parseColor("#137333")
                for (dx in listOf(-7f, 0f, 7f)) {
                    val x = cx + dx * density
                    canvas.drawRoundRect(
                        RectF(x - 1.8f * density, cy - 13 * density, x + 1.8f * density, cy + 13 * density),
                        1.5f * density, 1.5f * density, paint
                    )
                }
                val leaf = Path().apply {
                    moveTo(cx, cy - 10 * density)
                    quadTo(cx + 14 * density, cy - 18 * density, cx + 16 * density, cy - 6 * density)
                    close()
                }
                canvas.drawPath(leaf, paint)
            }
            c.contains("rice") -> {
                paint.color = Color.parseColor("#F9AB00")
                for (i in 0..4) {
                    val gx = cx + (i * 2.6f - 5f) * density
                    val gy = cy - (i * 3.0f - 6f) * density
                    canvas.drawOval(RectF(gx - 3 * density, gy - 2 * density, gx + 3 * density, gy + 2 * density), paint)
                }
            }
            c.contains("corn") -> {
                paint.color = Color.parseColor("#FBC02D")
                canvas.drawOval(RectF(cx - 6 * density, cy - 12 * density, cx + 6 * density, cy + 12 * density), paint)
                paint.color = Color.parseColor("#34A853")
                val husk = Path().apply {
                    moveTo(cx - 7 * density, cy + 12 * density)
                    lineTo(cx - 10 * density, cy - 4 * density)
                    lineTo(cx, cy + 6 * density)
                    lineTo(cx + 10 * density, cy - 4 * density)
                    lineTo(cx + 7 * density, cy + 12 * density)
                    close()
                }
                canvas.drawPath(husk, paint)
            }
            c.contains("coconut") || c.contains("nipa") -> {
                paint.color = Color.parseColor("#6D4C41")
                canvas.drawRect(cx - 2 * density, cy - 2 * density, cx + 2 * density, cy + 14 * density, paint)
                paint.color = Color.parseColor("#007B83")
                for (angle in listOf(-30f, 15f, 165f, 210f)) {
                    canvas.save()
                    canvas.rotate(angle, cx, cy - 4 * density)
                    canvas.drawOval(RectF(cx, cy - 7 * density, cx + 15 * density, cy - 1 * density), paint)
                    canvas.restore()
                }
                paint.color = Color.parseColor("#34A853")
                canvas.drawCircle(cx, cy - 1 * density, 3.5f * density, paint)
            }
            c.contains("banana") -> {
                paint.color = Color.parseColor("#FBC02D")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 4.2f * density
                canvas.drawArc(RectF(cx - 12 * density, cy - 12 * density, cx + 10 * density, cy + 10 * density), 15f, 125f, false, paint)
                canvas.drawArc(RectF(cx - 8 * density, cy - 15 * density, cx + 14 * density, cy + 7 * density), 15f, 125f, false, paint)
                paint.style = Paint.Style.FILL
            }
            c.contains("dragon fruit") -> {
                paint.color = Color.parseColor("#D81B60")
                canvas.drawOval(RectF(cx - 8 * density, cy - 11 * density, cx + 8 * density, cy + 12 * density), paint)
                paint.color = Color.parseColor("#2E7D32")
                val topScale = Path().apply {
                    moveTo(cx - 4 * density, cy - 10 * density)
                    lineTo(cx, cy - 17 * density)
                    lineTo(cx + 4 * density, cy - 10 * density)
                    close()
                }
                val leftScale = Path().apply {
                    moveTo(cx - 7 * density, cy - 2 * density)
                    lineTo(cx - 13 * density, cy - 7 * density)
                    lineTo(cx - 8 * density, cy + 3 * density)
                    close()
                }
                val rightScale = Path().apply {
                    moveTo(cx + 7 * density, cy - 2 * density)
                    lineTo(cx + 13 * density, cy - 7 * density)
                    lineTo(cx + 8 * density, cy + 3 * density)
                    close()
                }
                canvas.drawPath(topScale, paint)
                canvas.drawPath(leftScale, paint)
                canvas.drawPath(rightScale, paint)
                paint.color = Color.WHITE
                canvas.drawCircle(cx, cy + 1 * density, 5 * density, paint)
                paint.color = Color.parseColor("#202124")
                for ((sx, sy) in listOf(-2f to 0f, 2f to 0f, 0f to 2.5f, -1.5f to -2.5f, 1.5f to 3f)) {
                    canvas.drawCircle(cx + sx * density, cy + (1 + sy) * density, 0.9f * density, paint)
                }
            }
            c.contains("mango") || c.contains("cashew") -> {
                paint.color = Color.parseColor("#FB8C00")
                canvas.drawOval(RectF(cx - 8 * density, cy - 10 * density, cx + 8 * density, cy + 11 * density), paint)
                paint.color = Color.parseColor("#137333")
                canvas.drawOval(RectF(cx, cy - 14 * density, cx + 10 * density, cy - 8 * density), paint)
            }
            c.contains("pineapple") -> {
                paint.color = Color.parseColor("#F9AB00")
                canvas.drawOval(RectF(cx - 8 * density, cy - 5 * density, cx + 8 * density, cy + 13 * density), paint)
                paint.color = Color.parseColor("#137333")
                val crown = Path().apply {
                    moveTo(cx - 6 * density, cy - 5 * density)
                    lineTo(cx, cy - 16 * density)
                    lineTo(cx + 6 * density, cy - 5 * density)
                    close()
                }
                canvas.drawPath(crown, paint)
            }
            c.contains("coffee") || c.contains("cacao") -> {
                paint.color = Color.parseColor("#8D3B24")
                canvas.drawOval(RectF(cx - 9 * density, cy - 7 * density, cx + 2 * density, cy + 8 * density), paint)
                paint.color = Color.parseColor("#C5221F")
                canvas.drawOval(RectF(cx - 1 * density, cy - 5 * density, cx + 10 * density, cy + 9 * density), paint)
            }
            c.contains("cassava") || c.contains("root") || c.contains("potato") -> {
                paint.color = Color.parseColor("#A1887F")
                canvas.drawOval(RectF(cx - 12 * density, cy - 4 * density, cx + 6 * density, cy + 6 * density), paint)
                paint.color = Color.parseColor("#8D6E63")
                canvas.drawOval(RectF(cx - 3 * density, cy - 8 * density, cx + 13 * density, cy + 2 * density), paint)
            }
            c.contains("fishery") -> {
                paint.color = Color.parseColor("#0288D1")
                canvas.drawOval(RectF(cx - 11 * density, cy - 6 * density, cx + 7 * density, cy + 6 * density), paint)
                val tail = Path().apply {
                    moveTo(cx + 6 * density, cy)
                    lineTo(cx + 13 * density, cy - 6 * density)
                    lineTo(cx + 13 * density, cy + 6 * density)
                    close()
                }
                canvas.drawPath(tail, paint)
            }
            else -> {
                paint.color = Color.parseColor("#34A853")
                canvas.drawCircle(cx - 4 * density, cy + 2 * density, 8 * density, paint)
                paint.color = Color.parseColor("#EA4335")
                canvas.drawCircle(cx + 6 * density, cy + 3 * density, 6.5f * density, paint)
                paint.color = Color.parseColor("#7627BB")
                canvas.drawOval(RectF(cx - 3 * density, cy - 11 * density, cx + 5 * density, cy + 2 * density), paint)
            }
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#202124")
            textSize = 9.2f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val displayLabel = if (cropItemName.length > 15) cropItemName.take(14) + "…" else cropItemName
        canvas.drawText(displayLabel, cx, 71 * density, labelPaint)

        return bitmap
    }
}