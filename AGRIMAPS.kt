package com.example.myapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.data.NasugbuData
import com.example.myapplication.map.MapGraphics
import com.example.myapplication.map.MapTileSources
import com.example.myapplication.model.Farm
import com.example.myapplication.ui.AuthScreens
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var mapView: MapView
    private lateinit var mapContainer: FrameLayout
    private lateinit var topControlsContainer: LinearLayout
    private lateinit var btnToggleTopBar: Button
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
    private lateinit var subtitleText: TextView

    private lateinit var mainMapScreen: LinearLayout
    private lateinit var loginScreen: LinearLayout

    private lateinit var farms: MutableList<Farm>
    private var selectedFarm: Farm? = null
    private var tappedCustomPoint: GeoPoint? = null
    private var activeCropFilter: String = "ALL"
    private var activeSearchQuery: String = ""
    private var showMapLabels: Boolean = false
    private var isTopBarHidden: Boolean = false
    private var mapModeIndex: Int = 0
    private val filterButtons = mutableMapOf<String, Button>()
    private var splashDismissed = false
    private var currentUserRole: String = "Guest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        farms = NasugbuData.getConsolidatedFarms()

        title = "Nasugbu Agricultural Map"
        val logoBitmap = MapGraphics.loadAppLogoBitmap(this)
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
        osmConfig.userAgentValue = "NasugbuAgriculturalMap/2026.12 (Android; Batangas)"
        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache = File(osmConfig.osmdroidBasePath, "tiles")

        val rootContainer = FrameLayout(this)

        mainMapScreen = buildMainMapScreen()
        loginScreen = AuthScreens.buildLoginScreen(this) { role, subtitleLabel ->
            currentUserRole = role
            subtitleText.text = subtitleLabel
            loginScreen.visibility = View.GONE
            mapView.invalidate()
        }
        lateinit var splashScreen: LinearLayout
        splashScreen = AuthScreens.buildSplashScreen(this) {
            dismissSplashScreen(rootContainer, splashScreen)
        }

        rootContainer.addView(
            mainMapScreen,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )
        rootContainer.addView(
            loginScreen,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )
        rootContainer.addView(
            splashScreen,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )

        setContentView(rootContainer)

        Handler(Looper.getMainLooper()).postDelayed({
            dismissSplashScreen(rootContainer, splashScreen)
        }, 2000L)
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

    private fun buildMainMapScreen(): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.parseColor("#F8F9FA"))
        }

        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 10, 14, 10)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 20f
                setStroke(2, Color.parseColor("#E8EAED"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        }

        val headerLogo = ImageView(this).apply {
            setImageBitmap(MapGraphics.loadAppLogoBitmap(this@MainActivity))
            scaleType = ImageView.ScaleType.FIT_CENTER
            val logoSizePx = (40 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(logoSizePx, logoSizePx).apply {
                setMargins(0, 0, 10, 0)
            }
            setOnClickListener {
                hideFarmInfoPanel()
                plotFarmsOnMap(autoZoomSingleMatch = false)
                mapView.controller.animateTo(NasugbuData.center, 12.6, 500L)
            }
        }

        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val title = TextView(this).apply {
            text = "NASUGBU AGRICULTURAL MAP"
            textSize = 13.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#202124"))
        }

        subtitleText = TextView(this).apply {
            text = "Pindutin kahit saan sa mapa para sa GPS coordinates"
            textSize = 10f
            setTextColor(Color.parseColor("#137333"))
            setTypeface(null, Typeface.BOLD)
        }

        titleColumn.addView(title)
        titleColumn.addView(subtitleText)

        btnToggleTopBar = Button(this).apply {
            text = "HIDE BAR ▲"
            textSize = 9f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1A73E8"))
            setPadding(14, 6, 14, 6)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E8F0FE"))
                cornerRadius = 14f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(6, 0, 0, 0)
            }
            setOnClickListener { toggleTopControlsBar() }
        }

        val btnLogout = Button(this).apply {
            text = "SIGN IN"
            textSize = 9f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(14, 6, 14, 6)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#D93025"))
                cornerRadius = 14f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(6, 0, 0, 0)
            }
            setOnClickListener {
                hideFarmInfoPanel()
                loginScreen.visibility = View.VISIBLE
            }
        }

        headerRow.addView(headerLogo)
        headerRow.addView(titleColumn)
        headerRow.addView(btnToggleTopBar)
        headerRow.addView(btnLogout)

        topControlsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

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
                mapView.controller.animateTo(NasugbuData.center, 12.6, 500L)
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
            text = "SATELLITE 3D"
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
                        mapView.setMapOrientation(0f)
                        text = "SATELLITE 3D"
                        setBackgroundColor(Color.parseColor("#137333"))
                    }
                    1 -> {
                        mapView.setTileSource(MapTileSources.satelliteHybrid2026)
                        mapView.controller.setZoom(14.5)
                        text = "TERRAIN 2026"
                        setBackgroundColor(Color.parseColor("#0B57D0"))
                        Toast.makeText(
                            this@MainActivity,
                            "Hybrid Satellite View Active (2026 Imagery)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        mapView.setTileSource(MapTileSources.terrain2026)
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

        topControlsContainer.addView(searchRow)
        topControlsContainer.addView(filterScroll)
        topControlsContainer.addView(buttonScroll)

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
            setScrollableAreaLimitDouble(NasugbuData.bounds)
            minZoomLevel = 12.2
            maxZoomLevel = 19.0
            controller.setZoom(12.6)
            controller.setCenter(NasugbuData.center)
        }

        val rotationOverlay = RotationGestureOverlay(mapView).apply { isEnabled = true }
        mapView.overlays.add(rotationOverlay)

        val mapTapCoordinatesOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                InfoWindow.closeAllInfoWindowsOn(mapView)
                if (p != null) showCustomLocationPanel(p)
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                if (p != null) showCustomLocationPanel(p)
                return true
            }
        })
        mapView.overlays.add(mapTapCoordinatesOverlay)

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
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )

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
            setPadding(16, 4, 16, 4)
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

        val actionButtonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        btnGoogleMapsArea = Button(this).apply {
            text = "MAPS"
            textSize = 10.5f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#1A73E8"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 6, 0)
            }
            setOnClickListener {
                getActiveTargetCoordinates()?.let { (lat, lng) ->
                    openCoordinatesInGoogleMaps(lat, lng, satellite3DMode = false)
                }
            }
        }

        btnDirections = Button(this).apply {
            text = "DIRECTIONS"
            textSize = 10.5f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#137333"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 6, 0)
            }
            setOnClickListener {
                getActiveTargetCoordinates()?.let { (lat, lng) ->
                    openCoordinatesDirections(lat, lng)
                }
            }
        }

        btnStreetView = Button(this).apply {
            text = "3D SATELLITE"
            textSize = 10.5f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#E37400"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                getActiveTargetCoordinates()?.let { (lat, lng) ->
                    openCoordinatesInGoogleMaps(lat, lng, satellite3DMode = true)
                }
            }
        }

        actionButtonsRow.addView(btnGoogleMapsArea)
        actionButtonsRow.addView(btnDirections)
        actionButtonsRow.addView(btnStreetView)

        infoPanel.addView(infoHeaderRow)
        infoPanel.addView(resultScroll)
        infoPanel.addView(actionButtonsRow)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                activeSearchQuery = s?.toString()?.trim() ?: ""
                plotFarmsOnMap(autoZoomSingleMatch = true)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        layout.addView(headerRow)
        layout.addView(topControlsContainer)
        layout.addView(mapContainer)
        layout.addView(infoPanel)

        plotFarmsOnMap(autoZoomSingleMatch = false)
        return layout
    }

    private fun getActiveTargetCoordinates(): Pair<Double, Double>? {
        selectedFarm?.let { return Pair(it.lat, it.lng) }
        tappedCustomPoint?.let { return Pair(it.latitude, it.longitude) }
        return null
    }

    private fun toggleTopControlsBar() {
        isTopBarHidden = !isTopBarHidden
        if (isTopBarHidden) {
            topControlsContainer.visibility = View.GONE
            btnToggleTopBar.text = "SHOW BAR ▼"
            btnToggleTopBar.setTextColor(Color.WHITE)
            btnToggleTopBar.background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A73E8"))
                cornerRadius = 14f
            }
        } else {
            topControlsContainer.visibility = View.VISIBLE
            btnToggleTopBar.text = "HIDE BAR ▲"
            btnToggleTopBar.setTextColor(Color.parseColor("#1A73E8"))
            btnToggleTopBar.background = GradientDrawable().apply {
                setColor(Color.parseColor("#E8F0FE"))
                cornerRadius = 14f
            }
        }
        mapView.invalidate()
    }

    private fun updateFilterChipStyles() {
        for ((key, btn) in filterButtons) {
            val isSelected = key == activeCropFilter
            btn.setBackgroundColor(if (isSelected) Color.parseColor("#137333") else Color.parseColor("#E8EAED"))
            btn.setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#3C4043"))
        }
    }

    private fun enforceNasugbuOnlyCamera() {
        val center = mapView.mapCenter
        val clampedLat = center.latitude.coerceIn(NasugbuData.bounds.latSouth, NasugbuData.bounds.latNorth)
        val clampedLng = center.longitude.coerceIn(NasugbuData.bounds.lonWest, NasugbuData.bounds.lonEast)
        if (kotlin.math.abs(center.latitude - clampedLat) > 0.0002 ||
            kotlin.math.abs(center.longitude - clampedLng) > 0.0002
        ) {
            mapView.controller.setCenter(GeoPoint(clampedLat, clampedLng))
        }
    }

    private fun plotFarmsOnMap(autoZoomSingleMatch: Boolean, highlightedFarmName: String? = null) {
        InfoWindow.closeAllInfoWindowsOn(mapView)
        mapView.overlays.removeAll { it is Marker || it is Polygon }

        val nasugbuBoundaryOutline = Polygon(mapView).apply {
            points = NasugbuData.polygonPoints
            fillPaint.color = Color.argb(14, 19, 115, 51)
            outlinePaint.color = Color.parseColor("#137333")
            outlinePaint.strokeWidth = 5.0f
            infoWindow = null
            setOnClickListener { _, _, _ -> false }
        }
        mapView.overlays.add(nasugbuBoundaryOutline)

        val queryLower = activeSearchQuery.lowercase(Locale.US)
        val visibleFarms = farms.filter { farm ->
            val matchesText = queryLower.isEmpty() ||
                    farm.name.lowercase(Locale.US).contains(queryLower) ||
                    farm.crop.lowercase(Locale.US).contains(queryLower)
            matchesText && NasugbuData.matchesCropCategory(farm, activeCropFilter)
        }

        if (visibleFarms.isEmpty()) {
            if (tappedCustomPoint == null) hideFarmInfoPanel()
            drawCustomTappedPinIfPresent()
            mapView.invalidate()
            return
        }

        for (farm in visibleFarms) {
            val point = GeoPoint(farm.lat, farm.lng)
            val accentColor = MapGraphics.getCropAccentColor(farm.crop)
            val isSelected = highlightedFarmName != null && farm.name.equals(highlightedFarmName, ignoreCase = true)

            val marker = Marker(mapView).apply {
                position = point
                isDraggable = false
                infoWindow = null
                icon = if (showMapLabels || isSelected) {
                    MapGraphics.createFloatingInfoCardDrawable(this@MainActivity, farm, accentColor, isSelected)
                } else {
                    MapGraphics.createBarangayPinDrawable(this@MainActivity, accentColor)
                }
                setAnchor(
                    Marker.ANCHOR_CENTER,
                    if (showMapLabels || isSelected) Marker.ANCHOR_CENTER else Marker.ANCHOR_BOTTOM
                )
                setOnMarkerClickListener { _, _ ->
                    showFarmInfoPanel(farm)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        drawCustomTappedPinIfPresent()

        if (autoZoomSingleMatch && visibleFarms.size == 1) {
            val match = visibleFarms.first()
            mapView.controller.animateTo(GeoPoint(match.lat, match.lng), 15.6, 550L)
            showFarmInfoPanel(match)
        }

        mapView.invalidate()
    }

    private fun drawCustomTappedPinIfPresent() {
        val customPoint = tappedCustomPoint ?: return
        val blueAccent = Color.parseColor("#1A73E8")

        val customMarker = Marker(mapView).apply {
            position = customPoint
            isDraggable = false
            infoWindow = null
            icon = MapGraphics.createCustomCoordinateBadgeDrawable(this@MainActivity, customPoint, blueAccent)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ -> true }
        }
        mapView.overlays.add(customMarker)
    }

    private fun showFarmInfoPanel(farm: Farm) {
        InfoWindow.closeAllInfoWindowsOn(mapView)
        tappedCustomPoint = null
        selectedFarm = farm
        infoLabel.setTextColor(Color.parseColor("#137333"))
        infoLabel.text = "BRGY. ${farm.name.uppercase(Locale.US)} — DETAILS & GPS"
        result.text = farm.displayInfo()
        infoPanel.visibility = View.VISIBLE
        plotFarmsOnMap(autoZoomSingleMatch = false, highlightedFarmName = farm.name)
    }

    private fun showCustomLocationPanel(point: GeoPoint) {
        InfoWindow.closeAllInfoWindowsOn(mapView)
        selectedFarm = null
        tappedCustomPoint = point

        val townProper = GeoPoint(14.0728, 120.6336)
        val distKm = townProper.distanceToAsDouble(point) / 1000.0
        val latStr = String.format(Locale.US, "%.6f", point.latitude)
        val lngStr = String.format(Locale.US, "%.6f", point.longitude)

        infoLabel.setTextColor(Color.parseColor("#1A73E8"))
        infoLabel.text = "PINNED SPOT — EXACT GPS COORDINATES"
        result.text = "Location: Nasugbu, Batangas\n" +
                "Latitude: $latStr° N\n" +
                "Longitude: $lngStr° E\n" +
                "Decimal GPS: $latStr, $lngStr\n" +
                "Distance: ${String.format(Locale.US, "%.2f", distKm)} km from Town Proper"
        infoPanel.visibility = View.VISIBLE

        plotFarmsOnMap(autoZoomSingleMatch = false, highlightedFarmName = null)
    }

    private fun hideFarmInfoPanel() {
        InfoWindow.closeAllInfoWindowsOn(mapView)
        selectedFarm = null
        tappedCustomPoint = null
        infoPanel.visibility = View.GONE
        plotFarmsOnMap(autoZoomSingleMatch = false, highlightedFarmName = null)
    }

    private fun openCoordinatesInGoogleMaps(lat: Double, lng: Double, satellite3DMode: Boolean) {
        try {
            val basemap = if (satellite3DMode) "satellite" else "roadmap"
            val zoomLevel = if (satellite3DMode) 18 else 16
            val universalUrl = "https://www.google.com/maps/@?api=1&map_action=map&center=$lat,$lng&zoom=$zoomLevel&basemap=$basemap"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(universalUrl)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Walang naka-install na Google Maps o Browser sa device.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Hindi mabuksan ang mapa: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCoordinatesDirections(lat: Double, lng: Double) {
        try {
            val directionsUrl = "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(directionsUrl)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Walang naka-install na Google Maps o Browser para sa directions.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Hindi mabuksan ang directions: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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