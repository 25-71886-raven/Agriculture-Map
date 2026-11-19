package com.example.myapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
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
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.sqrt

class Farm(
    val name: String,
    val crop: String,
    val squareMeters: Long,
    val lat: Double,
    val lng: Double,
    val mergedParcels: Int = 1
) {
    fun shortAreaLabel(): String {
        val hectares = squareMeters / 10_000.0
        val formattedSqm = NumberFormat.getNumberInstance(Locale.US).format(squareMeters)
        return String.format(Locale.US, "%.1f ha (%s sqm)", hectares, formattedSqm)
    }

    fun formattedArea(): String {
        val formattedSqm = NumberFormat.getNumberInstance(Locale.US).format(squareMeters)
        val hectares = squareMeters / 10_000.0
        val formattedHa = String.format(Locale.US, "%.1f", hectares)
        return "$formattedHa ha ($formattedSqm sq. m)"
    }

    fun distanceFromTownKm(): String {
        val townProper = GeoPoint(14.0728, 120.6336)
        val barangayPoint = GeoPoint(lat, lng)
        val meters = townProper.distanceToAsDouble(barangayPoint)
        return String.format(Locale.US, "%.1f km from Town Proper", meters / 1000.0)
    }

    fun formattedLat(): String = String.format(Locale.US, "%.4f", lat)
    fun formattedLng(): String = String.format(Locale.US, "%.4f", lng)

    fun displayInfo(): String {
        val parcelNote = if (mergedParcels > 1) " ($mergedParcels combined records)" else ""
        return "Brgy. $name, Nasugbu, Batangas$parcelNote\n" +
                "Crops: $crop\n" +
                "Land Area: ${formattedArea()}\n" +
                "Distance: ${distanceFromTownKm()}  |  GPS: ${formattedLat()}° N, ${formattedLng()}° E"
    }
}

class MainActivity : Activity() {

    private lateinit var mapView: MapView
    private lateinit var mapContainer: FrameLayout
    private lateinit var searchInput: EditText
    private lateinit var btnToggleLabels: Button
    private lateinit var btnStreetView: Button
    private lateinit var btnGoogleMapsArea: Button
    private lateinit var btnDirections: Button
    private lateinit var btnCloseInfo: Button
    private lateinit var infoPanel: LinearLayout
    private lateinit var infoLabel: TextView
    private lateinit var result: TextView
    private lateinit var resultScroll: ScrollView

    private lateinit var mainMapScreen: LinearLayout

    private var selectedFarm: Farm? = null
    private var activeCropFilter: String = "ALL"
    private var activeSearchQuery: String = ""
    private var showMapLabels: Boolean = false
    private var mapModeIndex: Int = 0
    private val filterButtons = mutableMapOf<String, Button>()
    private var splashDismissed = false

    // Mahigpit na hangganan (boundary) ng buong Nasugbu, Batangas lamang
    private val nasugbuBounds = BoundingBox(
        14.2320,  // North
        120.8080, // East
        14.0220,  // South
        120.5920  // West
    )

    private val nasugbuCenter = GeoPoint(14.1150, 120.6950)

    private val nasugbuPolygonPoints = listOf(
        GeoPoint(14.2240, 120.6140),
        GeoPoint(14.2120, 120.6950),
        GeoPoint(14.1520, 120.7780),
        GeoPoint(14.1020, 120.8020),
        GeoPoint(14.0620, 120.7760),
        GeoPoint(14.0300, 120.6980),
        GeoPoint(14.0320, 120.6220),
        GeoPoint(14.0720, 120.6180),
        GeoPoint(14.1050, 120.6080),
        GeoPoint(14.1520, 120.6060),
        GeoPoint(14.1980, 120.6040),
        GeoPoint(14.2240, 120.6140)
    )

    private val satelliteTileSource = object : OnlineTileSourceBase(
        "EsriWorldImagery",
        0,
        16,
        256,
        ".jpg",
        arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/")
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val z = MapTileIndex.getZoom(pMapTileIndex)
            val x = MapTileIndex.getX(pMapTileIndex)
            val y = MapTileIndex.getY(pMapTileIndex)
            return "$baseUrl$z/$y/$x"
        }
    }

    private fun createDefaultRawFarms(): List<Farm> = listOf(
        // Northern & Western Coastal Barangays
        Farm("Papaya", "Coconut, Mango & Banana", 2_100_000L, 14.1992, 120.6178),
        Farm("Looc", "Coconut, Upland Rice & Banana", 2_750_000L, 14.1658, 120.6335),
        Farm("Calayo", "Coconut, Root Crops & Coastal Fishery", 1_900_000L, 14.1502, 120.6172),
        Farm("Natipuan", "Coconut, Mango & Cashew", 2_350_000L, 14.1305, 120.6228),
        Farm("Balaytigue", "Coconut, Cashew & Banana", 2_450_000L, 14.1112, 120.6258),
        Farm("Bulihan", "Corn, Vegetables & Coconut", 2_900_000L, 14.1585, 120.6545),
        Farm("Wawa", "Coastal Fishery, Nipa & Salt/Crops", 1_450_000L, 14.0868, 120.6258),
        Farm("Pantalan", "Irrigated Rice & Leafy Vegetables", 2_250_000L, 14.0815, 120.6315),
        Farm("Bucana", "Nipa, Coconut & Estuarine Crops", 1_150_000L, 14.0668, 120.6318),

        // Central Sugarcane & Irrigated Rice Plains
        Farm("Putat", "Sugarcane, Coconut & Cacao", 2_600_000L, 14.0825, 120.6585),
        Farm("Dayap", "Sugarcane & Corn", 3_800_000L, 14.0985, 120.6655),
        Farm("Lumbangan", "Sugarcane & Irrigated Rice", 3_500_000L, 14.0738, 120.6462),
        Farm("Reparo", "Irrigated Rice & Sugarcane", 2_550_000L, 14.0695, 120.6885),
        Farm("Bunducan", "Sugarcane & Banana", 2_950_000L, 14.0952, 120.6430),
        Farm("Banilad", "Irrigated Rice & Sugarcane", 4_200_000L, 14.0852, 120.7338),
        Farm("Malapad na Bato", "Sugarcane & Corn", 3_150_000L, 14.1105, 120.6885),

        // Eastern Uplands & Tagaytay Ridge
        Farm("Mataas na Pulo", "Coffee, Cacao & Banana", 3_250_000L, 14.1322, 120.7215),
        Farm("Utod", "Corn, Cassava & Saba Banana", 3_100_000L, 14.1245, 120.6638),
        Farm("Aga", "Highland Vegetables, Coffee & Sayote", 3_400_000L, 14.0949, 120.7963),
        Farm("Kaylaway", "Pineapple, Coffee & Banana", 3_600_000L, 14.0982, 120.7795),
        Farm("Kayrilaw", "Pineapple, Sweet Potato & Coffee", 2_950_000L, 14.1018, 120.7865),
        Farm("Tumalim", "Sugarcane, Upland Rice & Banana", 3_300_000L, 14.0768, 120.7525),

        // Southern Barangays
        Farm("Latag", "Sugarcane & Lowland Rice", 2_850_000L, 14.0585, 120.6815),
        Farm("Catandaan", "Irrigated Rice & Vegetables", 2_400_000L, 14.0495, 120.6458),
        Farm("Talangan", "Irrigated Rice & Watermelon", 2_300_000L, 14.0755, 120.6305),
        Farm("San Diego", "Coconut, Rice & Bamboo", 2_150_000L, 14.0352, 120.6335),
        Farm("Maugat", "Sugarcane, Rice & Mango", 2_700_000L, 14.0835, 120.6785),
        Farm("Camastilisan", "Sugarcane & Irrigated Rice", 2_650_000L, 14.0422, 120.6732),
        Farm("Cogunan", "Mango Orchards, Sugarcane & Banana", 3_100_000L, 14.0815, 120.6615),
        Farm("Bilaran", "Sugarcane & Corn", 3_000_000L, 14.0492, 120.6775)
    )

    private lateinit var farms: MutableList<Farm>

    private fun consolidateFarmsByBarangay(records: List<Farm>): List<Farm> {
        val grouped = LinkedHashMap<String, MutableList<Farm>>()
        for (record in records) {
            val normalizedKey = record.name.trim()
                .removePrefix("Brgy.")
                .removePrefix("Barangay")
                .trim()
                .lowercase(Locale.US)
            grouped.getOrPut(normalizedKey) { mutableListOf() }.add(record)
        }

        return grouped.values.map { entries ->
            val primary = entries.first()
            val cleanName = primary.name.trim()
                .removePrefix("Brgy.")
                .removePrefix("Barangay")
                .trim()

            val totalSqm = entries.sumOf { it.squareMeters }

            val uniqueCrops = LinkedHashSet<String>()
            for (item in entries) {
                item.crop.split(",", "&", "/")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { cropName ->
                        if (uniqueCrops.none { it.equals(cropName, ignoreCase = true) }) {
                            uniqueCrops.add(cropName)
                        }
                    }
            }

            val combinedCropText = when (uniqueCrops.size) {
                0 -> primary.crop
                1 -> uniqueCrops.first()
                else -> {
                    val list = uniqueCrops.toList()
                    list.dropLast(1).joinToString(", ") + " & " + list.last()
                }
            }

            Farm(
                name = cleanName,
                crop = combinedCropText,
                squareMeters = totalSqm,
                lat = primary.lat,
                lng = primary.lng,
                mergedParcels = entries.size
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        farms = consolidateFarmsByBarangay(createDefaultRawFarms()).toMutableList()

        title = "Nasugbu Agricultural Map"
        val logoBitmap = loadAppLogoBitmap()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setTaskDescription(
                ActivityManager.TaskDescription(
                    "Nasugbu Agricultural Map",
                    R.mipmap.ic_launcher,
                    Color.parseColor("#137333")
                )
            )
        } else {
            @Suppress("DEPRECATION")
            setTaskDescription(
                ActivityManager.TaskDescription(
                    "Nasugbu Agricultural Map",
                    logoBitmap,
                    Color.parseColor("#137333")
                )
            )
        }

        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = "NasugbuAgriculturalMap/9.6 (Android; Batangas)"
        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache = File(osmConfig.osmdroidBasePath, "tiles")

        val rootContainer = FrameLayout(this)

        mainMapScreen = buildMainMapScreen()
        val splashScreen = buildStartingLogoScreen(rootContainer)

        rootContainer.addView(
            mainMapScreen,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        rootContainer.addView(
            splashScreen,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(rootContainer)

        Handler(Looper.getMainLooper()).postDelayed({
            dismissSplashScreen(rootContainer, splashScreen)
        }, 2000L)
    }

    private fun loadAppLogoBitmap(): Bitmap {
        val customDrawableId = resources.getIdentifier("app_logo", "drawable", packageName)
        if (customDrawableId != 0) {
            val decoded = BitmapFactory.decodeResource(resources, customDrawableId)
            if (decoded != null) return decoded
        }
        return createMapLogoBitmap()
    }

    private fun buildStartingLogoScreen(rootContainer: FrameLayout): LinearLayout {
        val splashLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#0D47A1"),
                    Color.parseColor("#137333")
                )
            )
            isClickable = true
            isFocusable = true
        }

        val logoView = ImageView(this).apply {
            setImageBitmap(loadAppLogoBitmap())
            scaleType = ImageView.ScaleType.FIT_CENTER
            val sizePx = (150 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                setMargins(0, 0, 0, 24)
            }
            scaleX = 0.82f
            scaleY = 0.82f
            alpha = 0f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600L)
                .start()
        }

        val appTitle = TextView(this).apply {
            text = "NASUGBU AGRICULTURAL MAP"
            textSize = 23f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            letterSpacing = 0.04f
        }

        val appSubtitle = TextView(this).apply {
            text = "Municipal Crop & Barangay Land Area Map\nNasugbu, Batangas"
            textSize = 14f
            setTextColor(Color.parseColor("#E8F5E9"))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }

        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val btnContinue = Button(this).apply {
            text = "CONTINUE TO MAP"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#137333"))
            setPadding(44, 18, 44, 18)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 50f
            }
            setOnClickListener {
                dismissSplashScreen(rootContainer, splashLayout)
            }
        }

        splashLayout.addView(logoView)
        splashLayout.addView(appTitle)
        splashLayout.addView(appSubtitle)
        splashLayout.addView(progressBar)
        splashLayout.addView(btnContinue)

        return splashLayout
    }

    private fun dismissSplashScreen(rootContainer: FrameLayout, splashView: View) {
        if (splashDismissed) return
        splashDismissed = true

        splashView.animate()
            .alpha(0f)
            .setDuration(350L)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootContainer.removeView(splashView)
                    mapView.invalidate()
                }
            })
            .start()
    }

    private fun createMapLogoBitmap(): Bitmap {
        val density = resources.displayMetrics.density
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

    private fun buildMainMapScreen(): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.parseColor("#F8F9FA"))
        }

        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 12, 16, 12)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 20f
                setStroke(2, Color.parseColor("#E8EAED"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        }

        val headerLogo = ImageView(this).apply {
            setImageBitmap(loadAppLogoBitmap())
            scaleType = ImageView.ScaleType.FIT_CENTER
            val logoSizePx = (44 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(logoSizePx, logoSizePx).apply {
                setMargins(0, 0, 14, 0)
            }
            setOnClickListener {
                hideFarmInfoPanel()
                plotFarmsOnMap(autoZoomSingleMatch = false)
                mapView.controller.animateTo(nasugbuCenter, 12.4, 500L)
            }
        }

        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val title = TextView(this).apply {
            text = "NASUGBU AGRICULTURAL MAP"
            textSize = 15.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#202124"))
        }

        val subtitle = TextView(this).apply {
            text = "Pindutin ang barangay pin para makita ang detalye"
            textSize = 11f
            setTextColor(Color.parseColor("#137333"))
            setTypeface(null, Typeface.BOLD)
        }

        titleColumn.addView(title)
        titleColumn.addView(subtitle)

        headerRow.addView(headerLogo)
        headerRow.addView(titleColumn)

        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        }

        searchInput = EditText(this).apply {
            hint = "Search barangay or crop (e.g. Lumbangan, Dayap, Tumalim)..."
            textSize = 13f
            setSingleLine(true)
            setPadding(24, 18, 24, 18)
            setBackgroundColor(Color.WHITE)
            setTextColor(Color.parseColor("#202124"))
            setHintTextColor(Color.parseColor("#80868B"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 6, 0)
            }
        }

        val btnClearSearch = Button(this).apply {
            text = "CLEAR"
            textSize = 10.5f
            setBackgroundColor(Color.parseColor("#5F6368"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                searchInput.setText("")
                hideFarmInfoPanel()
            }
        }

        searchRow.addView(searchInput)
        searchRow.addView(btnClearSearch)

        val filterScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        }

        val filterRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val categories = listOf(
            "ALL" to "ALL (${farms.size})",
            "SUGARCANE" to "SUGARCANE",
            "RICE" to "RICE",
            "COCONUT" to "COCONUT",
            "FRUIT" to "PINEAPPLE / MANGO",
            "VEG" to "CORN / VEG"
        )

        for ((key, label) in categories) {
            val chipBtn = Button(this).apply {
                text = label
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setPadding(22, 8, 22, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 8, 0)
                }
                setOnClickListener {
                    activeCropFilter = key
                    updateFilterChipStyles()
                    plotFarmsOnMap(autoZoomSingleMatch = true)
                }
            }
            filterButtons[key] = chipBtn
            filterRow.addView(chipBtn)
        }
        filterScroll.addView(filterRow)
        updateFilterChipStyles()

        val buttonScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val btnDisplay = Button(this).apply {
            text = "CENTER"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#1A73E8"))
            setTextColor(Color.WHITE)
            setPadding(20, 10, 20, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 6, 0)
            }
            setOnClickListener {
                hideFarmInfoPanel()
                mapView.controller.animateTo(nasugbuCenter, 12.4, 500L)
            }
        }

        btnToggleLabels = Button(this).apply {
            text = "SHOW BRGY INFO"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#5F6368"))
            setTextColor(Color.WHITE)
            setPadding(20, 10, 20, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 6, 0)
            }
            setOnClickListener {
                showMapLabels = !showMapLabels
                text = if (showMapLabels) "HIDE BRGY INFO" else "SHOW BRGY INFO"
                setBackgroundColor(
                    if (showMapLabels) Color.parseColor("#E37400") else Color.parseColor("#5F6368")
                )
                plotFarmsOnMap(autoZoomSingleMatch = false, highlightedFarmName = selectedFarm?.name)
            }
        }

        val btnLayer = Button(this).apply {
            text = "SATELLITE"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#137333"))
            setTextColor(Color.WHITE)
            setPadding(20, 10, 20, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                mapModeIndex = (mapModeIndex + 1) % 3
                when (mapModeIndex) {
                    0 -> {
                        mapView.setTileSource(TileSourceFactory.MAPNIK)
                        text = "SATELLITE"
                        setBackgroundColor(Color.parseColor("#137333"))
                    }
                    1 -> {
                        mapView.setTileSource(satelliteTileSource)
                        text = "TERRAIN"
                        setBackgroundColor(Color.parseColor("#0B57D0"))
                    }
                    else -> {
                        mapView.setTileSource(TileSourceFactory.OpenTopo)
                        text = "ROAD MAP"
                        setBackgroundColor(Color.parseColor("#E37400"))
                    }
                }
                mapView.invalidate()
            }
        }

        buttonRow.addView(btnDisplay)
        buttonRow.addView(btnToggleLabels)
        buttonRow.addView(btnLayer)
        buttonScroll.addView(buttonRow)

        // Map Container & OSMDroid MapView
        mapContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        mapView = MapView(this).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setScrollableAreaLimitDouble(nasugbuBounds)
            minZoomLevel = 11.5
            maxZoomLevel = 18.0
            controller.setZoom(12.4)
            controller.setCenter(nasugbuCenter)
        }

        val rotationOverlay = RotationGestureOverlay(mapView).apply {
            isEnabled = true
        }
        mapView.overlays.add(rotationOverlay)

        // Kapag nag-tap sa bakanteng lugar sa mapa, isasara ang anumang InfoWindow
        // at itatago ang bottom details card (walang lalabas na white chat box).
        val emptyMapTapOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                InfoWindow.closeAllInfoWindowsOn(mapView)
                hideFarmInfoPanel()
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        })
        mapView.overlays.add(emptyMapTapOverlay)

        mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                enforceNasugbuOnlyCamera()
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                enforceNasugbuOnlyCamera()
                return true
            }
        })

        mapContainer.addView(
            mapView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Bottom Barangay Details Card
        infoPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(24, 18, 24, 18)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 24f
                setStroke(2, Color.parseColor("#DADCE0"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 0)
            }
        }

        val infoHeaderRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        infoLabel = TextView(this).apply {
            text = "BARANGAY AGRICULTURAL DETAILS"
            textSize = 11.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#137333"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        btnCloseInfo = Button(this).apply {
            text = "HIDE ✕"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#5F6368"))
            setBackgroundColor(Color.parseColor("#F1F3F4"))
            setPadding(18, 6, 18, 6)
            setOnClickListener { hideFarmInfoPanel() }
        }

        infoHeaderRow.addView(infoLabel)
        infoHeaderRow.addView(btnCloseInfo)

        resultScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 6, 0, 10)
            }
        }

        result = TextView(this).apply {
            textSize = 12.5f
            setTextColor(Color.parseColor("#202124"))
            setLineSpacing(4f, 1.15f)
        }
        resultScroll.addView(result)

        val actionScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        btnGoogleMapsArea = Button(this).apply {
            text = "MAPS"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#1A73E8"))
            setTextColor(Color.WHITE)
            setPadding(22, 10, 22, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 8, 0) }
            setOnClickListener {
                selectedFarm?.let { openInGoogleMaps(it, satelliteMode = false) }
            }
        }

        btnDirections = Button(this).apply {
            text = "DIRECTIONS"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#137333"))
            setTextColor(Color.WHITE)
            setPadding(22, 10, 22, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 8, 0) }
            setOnClickListener {
                selectedFarm?.let { openDirectionsInGoogleMaps(it) }
            }
        }

        btnStreetView = Button(this).apply {
            text = "SATELLITE"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#E37400"))
            setTextColor(Color.WHITE)
            setPadding(22, 10, 22, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                selectedFarm?.let { openInGoogleMaps(it, satelliteMode = true) }
            }
        }

        actionRow.addView(btnGoogleMapsArea)
        actionRow.addView(btnDirections)
        actionRow.addView(btnStreetView)
        actionScroll.addView(actionRow)

        infoPanel.addView(infoHeaderRow)
        infoPanel.addView(resultScroll)
        infoPanel.addView(actionScroll)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                activeSearchQuery = s?.toString()?.trim() ?: ""
                plotFarmsOnMap(autoZoomSingleMatch = true)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        layout.addView(headerRow)
        layout.addView(searchRow)
        layout.addView(filterScroll)
        layout.addView(buttonScroll)
        layout.addView(mapContainer)
        layout.addView(infoPanel)

        plotFarmsOnMap(autoZoomSingleMatch = false)
        return layout
    }

    private fun updateFilterChipStyles() {
        for ((key, btn) in filterButtons) {
            val isSelected = key == activeCropFilter
            btn.setBackgroundColor(
                if (isSelected) Color.parseColor("#137333") else Color.parseColor("#E8EAED")
            )
            btn.setTextColor(
                if (isSelected) Color.WHITE else Color.parseColor("#3C4043")
            )
        }
    }

    private fun enforceNasugbuOnlyCamera() {
        val center = mapView.mapCenter
        val clampedLat = center.latitude.coerceIn(nasugbuBounds.latSouth, nasugbuBounds.latNorth)
        val clampedLng = center.longitude.coerceIn(nasugbuBounds.lonWest, nasugbuBounds.lonEast)
        if (clampedLat != center.latitude || clampedLng != center.longitude) {
            mapView.controller.setCenter(GeoPoint(clampedLat, clampedLng))
        }
    }

    private fun matchesFilter(farm: Farm): Boolean {
        val cropLower = farm.crop.lowercase(Locale.US)
        val categoryMatches = when (activeCropFilter) {
            "ALL" -> true
            "SUGARCANE" -> cropLower.contains("sugarcane")
            "RICE" -> cropLower.contains("rice")
            "COCONUT" -> cropLower.contains("coconut")
            "FRUIT" -> cropLower.contains("pineapple") || cropLower.contains("mango") || cropLower.contains("banana")
            "VEG" -> cropLower.contains("corn") || cropLower.contains("vegetable") || cropLower.contains("sayote") || cropLower.contains("root")
            else -> true
        }
        if (!categoryMatches) return false

        if (activeSearchQuery.isEmpty()) return true
        val q = activeSearchQuery.lowercase(Locale.US)
        return farm.name.lowercase(Locale.US).contains(q) || farm.crop.lowercase(Locale.US).contains(q)
    }

    private fun getCropAccentColor(cropText: String): Int {
        val lower = cropText.lowercase(Locale.US)
        return when {
            lower.contains("sugarcane") -> Color.parseColor("#137333")
            lower.contains("rice") -> Color.parseColor("#E37400")
            lower.contains("coconut") -> Color.parseColor("#0B57D0")
            lower.contains("pineapple") || lower.contains("mango") -> Color.parseColor("#C5221F")
            else -> Color.parseColor("#7627BB")
        }
    }

    private fun plotFarmsOnMap(autoZoomSingleMatch: Boolean, highlightedFarmName: String? = null) {
        InfoWindow.closeAllInfoWindowsOn(mapView)

        val overlaysToRemove = mapView.overlays.filter { it is Marker || it is Polygon }
        mapView.overlays.removeAll(overlaysToRemove)

        // Municipal Boundary Outline (infoWindow = null at walang click bubble)
        val boundaryPolygon = Polygon(mapView).apply {
            points = nasugbuPolygonPoints
            fillPaint.color = Color.argb(18, 19, 115, 51)
            outlinePaint.color = Color.parseColor("#137333")
            outlinePaint.strokeWidth = 3.5f
            infoWindow = null
            setOnClickListener { _, _, _ ->
                hideFarmInfoPanel()
                false
            }
        }
        mapView.overlays.add(boundaryPolygon)

        val visibleFarms = farms.filter { matchesFilter(it) }

        if (visibleFarms.isEmpty()) {
            hideFarmInfoPanel()
            mapView.invalidate()
            return
        }

        for (farm in visibleFarms) {
            val point = GeoPoint(farm.lat, farm.lng)
            val accentColor = getCropAccentColor(farm.crop)
            val isHighlighted = highlightedFarmName != null && farm.name.equals(highlightedFarmName, ignoreCase = true)

            // Proportional Area Circle (infoWindow = null para hindi lumabas ang white chat bubble!)
            val radiusMeters = (sqrt(farm.squareMeters / Math.PI)).coerceIn(480.0, 1180.0)
            val areaCircle = Polygon(mapView).apply {
                points = Polygon.pointsAsCircle(point, radiusMeters)
                fillPaint.color = Color.argb(
                    if (isHighlighted) 75 else 42,
                    Color.red(accentColor),
                    Color.green(accentColor),
                    Color.blue(accentColor)
                )
                outlinePaint.color = accentColor
                outlinePaint.strokeWidth = if (isHighlighted) 4.0f else 2.0f
                infoWindow = null
                setOnClickListener { _, _, _ ->
                    showFarmInfoPanel(farm)
                    mapView.controller.animateTo(point, max(mapView.zoomLevelDouble, 14.2), 400L)
                    plotFarmsOnMap(autoZoomSingleMatch = false, highlightedFarmName = farm.name)
                    true
                }
            }
            mapView.overlays.add(areaCircle)

            // Barangay Pin Marker (Fixed — walang drag, walang infoWindow bubble)
            val marker = Marker(mapView).apply {
                position = point
                isDraggable = false
                infoWindow = null
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = createBarangayPinDrawable(farm, accentColor, isHighlighted)
                setOnMarkerClickListener { _, _ ->
                    showFarmInfoPanel(farm)
                    mapView.controller.animateTo(point, max(mapView.zoomLevelDouble, 14.2), 400L)
                    plotFarmsOnMap(autoZoomSingleMatch = false, highlightedFarmName = farm.name)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        if (autoZoomSingleMatch && visibleFarms.size == 1) {
            val single = visibleFarms.first()
            showFarmInfoPanel(single)
            mapView.controller.animateTo(GeoPoint(single.lat, single.lng), 15.6, 500L)
        }

        mapView.invalidate()
    }

    private fun createBarangayPinDrawable(farm: Farm, accentColor: Int, isHighlighted: Boolean): BitmapDrawable {
        val density = resources.displayMetrics.density
        val titleText = "Brgy. ${farm.name}"
        val subText = "${farm.shortAreaLabel()} • ${farm.crop}"

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 11f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E8F5E9")
            textSize = 9.5f * density
        }

        val pinRadius = (if (isHighlighted) 15f else 12f) * density
        val pinHeight = pinRadius * 2.5f

        if (!showMapLabels && !isHighlighted) {
            val w = (pinRadius * 2.6f).toInt().coerceAtLeast(1)
            val h = (pinHeight + 6 * density).toInt().coerceAtLeast(1)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawPinIcon(canvas, w / 2f, pinRadius + 2 * density, pinRadius, accentColor, false)
            return BitmapDrawable(resources, bmp)
        }

        val titleBounds = Rect()
        titlePaint.getTextBounds(titleText, 0, titleText.length, titleBounds)
        val subBounds = Rect()
        subPaint.getTextBounds(subText, 0, subText.length, subBounds)

        val padH = 10f * density
        val padV = 6f * density
        val cardWidth = max(titleBounds.width(), subBounds.width()) + padH * 2
        val cardHeight = titleBounds.height() + subBounds.height() + padV * 2.6f

        val totalWidth = max(cardWidth, pinRadius * 2.6f).toInt().coerceAtLeast(1)
        val totalHeight = (cardHeight + pinHeight + 8 * density).toInt().coerceAtLeast(1)

        val bmp = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val cardLeft = (totalWidth - cardWidth) / 2f
        val cardTop = 2f * density
        val cardRect = RectF(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight)

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isHighlighted) Color.parseColor("#202124") else accentColor
        }
        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 1.8f * density
        }

        canvas.drawRoundRect(cardRect, 10f * density, 10f * density, cardBgPaint)
        canvas.drawRoundRect(cardRect, 10f * density, 10f * density, cardBorderPaint)

        canvas.drawText(
            titleText,
            cardLeft + padH,
            cardTop + padV + titleBounds.height(),
            titlePaint
        )
        canvas.drawText(
            subText,
            cardLeft + padH,
            cardTop + padV * 1.7f + titleBounds.height() + subBounds.height(),
            subPaint
        )

        val pinCenterY = cardTop + cardHeight + 4f * density + pinRadius
        drawPinIcon(canvas, totalWidth / 2f, pinCenterY, pinRadius, accentColor, isHighlighted)

        return BitmapDrawable(resources, bmp)
    }

    private fun drawPinIcon(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        color: Int,
        highlighted: Boolean
    ) {
        val density = resources.displayMetrics.density
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = if (highlighted) Color.parseColor("#D93025") else color
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2.2f * density
        }

        val tail = Path().apply {
            moveTo(cx - radius * 0.78f, cy + radius * 0.35f)
            lineTo(cx + radius * 0.78f, cy + radius * 0.35f)
            lineTo(cx, cy + radius * 1.65f)
            close()
        }
        canvas.drawPath(tail, paint)
        canvas.drawCircle(cx, cy, radius, paint)
        canvas.drawCircle(cx, cy, radius, strokePaint)

        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, radius * 0.38f, paint)
    }

    private fun showFarmInfoPanel(farm: Farm) {
        selectedFarm = farm
        infoLabel.text = "BRGY. ${farm.name.uppercase(Locale.US)} — AGRICULTURAL DETAILS"
        result.text = farm.displayInfo()
        infoPanel.visibility = View.VISIBLE
    }

    private fun hideFarmInfoPanel() {
        selectedFarm = null
        infoPanel.visibility = View.GONE
    }

    private fun openInGoogleMaps(farm: Farm, satelliteMode: Boolean) {
        val uriString = if (satelliteMode) {
            "https://www.google.com/maps/@?api=1&map_action=map&center=${farm.lat},${farm.lng}&zoom=16&basemap=satellite"
        } else {
            "geo:${farm.lat},${farm.lng}?q=${farm.lat},${farm.lng}(Brgy.+${Uri.encode(farm.name)}+Nasugbu)"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            val webFallback = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${farm.lat},${farm.lng}")
            )
            startActivity(webFallback)
        }
    }

    private fun openDirectionsInGoogleMaps(farm: Farm) {
        val navUri = Uri.parse("google.navigation:q=${farm.lat},${farm.lng}")
        val intent = Intent(Intent.ACTION_VIEW, navUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            val webFallback = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${farm.lat},${farm.lng}")
            )
            startActivity(webFallback)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}