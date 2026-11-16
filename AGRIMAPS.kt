package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.text.NumberFormat
import java.util.Locale

class Farm(
    val name: String,
    val crop: String,
    val squareMeters: Long,
    val lat: Double,
    val lng: Double
) {
    fun formattedArea(): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(squareMeters)
        return "$formatted sq. m"
    }

    fun displayInfo(): String {
        return "Brgy. $name, Nasugbu, Batangas\nPrimary Crop: $crop  |  Area: ${formattedArea()}\nExact Coordinates: $lat° N, $lng° E"
    }
}

class MainActivity : Activity() {

    private lateinit var mapView: MapView
    private lateinit var mapContainer: FrameLayout
    private lateinit var btnStreetView: Button
    private lateinit var btnGoogleMapsArea: Button
    private lateinit var result: TextView
    private lateinit var resultScroll: ScrollView

    private var is3DTiltEnabled = false
    private var selectedFarm: Farm? = null

    // Eksaktong hangganan (boundary) ng buong Nasugbu, Batangas lamang
    private val nasugbuBounds = BoundingBox(
        14.2350,  // North
        120.8150, // East
        14.0150,  // South
        120.5750  // West
    )

    // Satellite Tile Source (Walang API Key na kailangan)
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

    // Eksaktong Google Maps position ng bawat Barangay sa Nasugbu (parehong lat/lng para sa farm at barangay)
    private val farms = arrayOf(
        Farm("Looc", "Coconut", 2_750_000L, 14.1588, 120.6282),
        Farm("Calayo", "Root Crops", 1_900_000L, 14.1482, 120.6195),
        Farm("Wawa", "Fishery / Coastal", 1_450_000L, 14.0864, 120.6238),
        Farm("Bulihan", "Corn", 2_900_000L, 14.1145, 120.6432),
        Farm("Pantalan", "Rice", 2_250_000L, 14.0912, 120.6324),
        Farm("Putat", "Coconut", 2_600_000L, 14.0795, 120.6625),
        Farm("Lumbangan", "Sugarcane", 3_500_000L, 14.0752, 120.6714),
        Farm("Dayap", "Sugarcane", 3_800_000L, 14.0816, 120.6981),
        Farm("Banilad", "Rice", 4_200_000L, 14.0844, 120.7294),
        Farm("Aga", "Vegetables", 3_400_000L, 14.1036, 120.7611),
        Farm("Kaylaway", "Pineapple", 3_600_000L, 14.0981, 120.7847),
        Farm("Cogunan", "Mango", 3_100_000L, 14.0586, 120.6891),
        Farm("Bilaran", "Corn", 3_000_000L, 14.0412, 120.6964)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = "NasugbuCropMapApp/3.1 (Android; Batangas)"

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.parseColor("#F8F9FA"))
        }

        val title = TextView(this).apply {
            text = "NASUGBU CROP MAP"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#202124"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val btnDisplay = Button(this).apply {
            text = "DISPLAY MAP"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#1A73E8"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 6, 0)
            }
        }

        val btnLayer = Button(this).apply {
            text = "SATELLITE"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#137333"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(6, 0, 6, 0)
            }
        }

        val btn3D = Button(this).apply {
            text = "3D TILT: OFF"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#5F6368"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(6, 0, 0, 0)
            }
        }

        buttonRow.addView(btnDisplay)
        buttonRow.addView(btnLayer)
        buttonRow.addView(btn3D)

        result = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#3C4043"))
            setPadding(20, 12, 20, 8)
            text = "Tap 'DISPLAY MAP' to plot all 13 Nasugbu barangays. Tap any barangay marker to open its exact location in Google Maps."
        }

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 4, 16, 12)
            }
        }

        btnGoogleMapsArea = Button(this).apply {
            text = "OPEN AREA IN GOOGLE MAPS"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#1A73E8"))
            setTextColor(Color.WHITE)
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 6, 0)
            }
        }

        btnStreetView = Button(this).apply {
            text = "360° STREET VIEW"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#D93025"))
            setTextColor(Color.WHITE)
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(6, 0, 0, 0)
            }
        }

        actionRow.addView(btnGoogleMapsArea)
        actionRow.addView(btnStreetView)

        val infoPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(result)
            addView(actionRow)
        }

        resultScroll = ScrollView(this).apply {
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                250
            )
            addView(infoPanel)
        }

        mapView = MapView(this).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            // Naka-lock sa buong Nasugbu, Batangas lamang
            setScrollableAreaLimitDouble(nasugbuBounds)
            minZoomLevel = 11.5
            maxZoomLevel = 16.5
            isTilesScaledToDpi = true
            setUseDataConnection(true)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false

            tileProvider.clearTileCache()
            controller.setZoom(12.2)
            controller.setCenter(GeoPoint(14.1150, 120.6950))
        }

        enableMapGesturesAndBoundary()

        // 3D Camera Perspective Tilt container
        mapContainer = object : FrameLayout(this) {
            private val camera3D = Camera()
            private val matrix3D = Matrix()

            override fun dispatchDraw(canvas: Canvas) {
                if (!is3DTiltEnabled) {
                    super.dispatchDraw(canvas)
                    return
                }

                canvas.save()
                val centerX = width / 2f
                val centerY = height / 2f

                camera3D.save()
                camera3D.rotateX(38f)
                camera3D.translate(0f, -height * 0.08f, -120f)
                camera3D.getMatrix(matrix3D)
                camera3D.restore()

                matrix3D.preTranslate(-centerX, -centerY)
                matrix3D.postTranslate(centerX, centerY)
                matrix3D.postScale(1.32f, 1.32f, centerX, centerY)

                canvas.concat(matrix3D)
                super.dispatchDraw(canvas)
                canvas.restore()
            }
        }.apply {
            setBackgroundColor(Color.parseColor("#E8EAED"))
            val mapParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            mapParams.setMargins(0, 16, 0, 16)
            layoutParams = mapParams
            addView(
                mapView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        btnDisplay.setOnClickListener {
            plotFarmsOnMap()
        }

        var mapModeIndex = 0
        btnLayer.setOnClickListener {
            mapModeIndex = (mapModeIndex + 1) % 3
            when (mapModeIndex) {
                0 -> {
                    mapView.maxZoomLevel = 16.5
                    if (mapView.zoomLevelDouble > 16.5) mapView.controller.setZoom(16.5)
                    mapView.setTileSource(TileSourceFactory.MAPNIK)
                    btnLayer.text = "SATELLITE"
                }
                1 -> {
                    mapView.maxZoomLevel = 16.5
                    if (mapView.zoomLevelDouble > 16.5) mapView.controller.setZoom(16.5)
                    mapView.setTileSource(satelliteTileSource)
                    btnLayer.text = "TERRAIN"
                }
                2 -> {
                    mapView.maxZoomLevel = 15.0
                    if (mapView.zoomLevelDouble > 15.0) mapView.controller.setZoom(15.0)
                    mapView.setTileSource(TileSourceFactory.OpenTopo)
                    btnLayer.text = "ROAD MAP"
                }
            }
            mapView.tileProvider.clearTileCache()
            mapView.invalidate()
        }

        btn3D.setOnClickListener {
            is3DTiltEnabled = !is3DTiltEnabled
            if (is3DTiltEnabled) {
                btn3D.text = "3D TILT: ON"
                btn3D.setBackgroundColor(Color.parseColor("#B06000"))
                if (mapView.zoomLevelDouble < 14.5) {
                    mapView.controller.animateTo(mapView.mapCenter, 15.0, 600L)
                }
            } else {
                btn3D.text = "3D TILT: OFF"
                btn3D.setBackgroundColor(Color.parseColor("#5F6368"))
                mapView.mapOrientation = 0f
            }
            mapContainer.invalidate()
            mapView.invalidate()
        }

        // Direktang bubukas sa eksaktong pin ng barangay sa Google Maps
        btnGoogleMapsArea.setOnClickListener {
            selectedFarm?.let { farm ->
                openDirectGoogleMapsArea(farm)
            }
        }

        // Direktang bubukas sa 360° Street View sa eksaktong lokasyon ng barangay
        btnStreetView.setOnClickListener {
            selectedFarm?.let { farm ->
                openDirectGoogleStreetView(farm)
            }
        }

        layout.addView(title)
        layout.addView(buttonRow)
        layout.addView(mapContainer)
        layout.addView(resultScroll)

        setContentView(layout)
    }

    private fun openDirectGoogleMapsArea(farm: Farm) {
        val label = Uri.encode("Brgy. ${farm.name} (${farm.crop})")
        val gmmIntentUri = Uri.parse("geo:0,0?q=${farm.lat},${farm.lng}($label)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            val webUrl = "https://www.google.com/maps/search/?api=1&query=${farm.lat},${farm.lng}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
        }
    }

    private fun openDirectGoogleStreetView(farm: Farm) {
        val streetUri = Uri.parse("google.streetview:cbll=${farm.lat},${farm.lng}")
        val streetIntent = Intent(Intent.ACTION_VIEW, streetUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (streetIntent.resolveActivity(packageManager) != null) {
            startActivity(streetIntent)
        } else {
            val webUrl = "https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=${farm.lat},${farm.lng}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
        }
    }

    private fun enableMapGesturesAndBoundary() {
        val rotationOverlay = RotationGestureOverlay(mapView).apply {
            isEnabled = true
        }
        mapView.overlays.add(rotationOverlay)
        drawNasugbuBoundary()
    }

    private fun drawNasugbuBoundary() {
        val boundaryPoints = listOf(
            GeoPoint(14.2180, 120.6140),
            GeoPoint(14.2050, 120.6950),
            GeoPoint(14.1450, 120.7750),
            GeoPoint(14.0980, 120.8020),
            GeoPoint(14.0620, 120.7720),
            GeoPoint(14.0280, 120.6980),
            GeoPoint(14.0320, 120.6240),
            GeoPoint(14.0720, 120.6290),
            GeoPoint(14.1050, 120.6120),
            GeoPoint(14.1520, 120.6090),
            GeoPoint(14.1920, 120.6050),
            GeoPoint(14.2180, 120.6140)
        )

        val polygon = Polygon(mapView).apply {
            points = boundaryPoints
            outlinePaint.color = Color.parseColor("#1A73E8")
            outlinePaint.strokeWidth = 5f
            fillPaint.color = Color.argb(25, 26, 115, 232)
            title = "Nasugbu, Batangas Boundary"
        }

        mapView.overlays.add(polygon)
    }

    private fun plotFarmsOnMap() {
        mapView.overlays.clear()
        enableMapGesturesAndBoundary()
        selectedFarm = null
        btnGoogleMapsArea.visibility = View.GONE
        btnStreetView.visibility = View.GONE

        var totalSqm = 0L
        val infoBuilder = StringBuilder()

        for (i in farms.indices) {
            val farm = farms[i]
            val point = GeoPoint(farm.lat, farm.lng)

            val marker = Marker(mapView).apply {
                position = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Brgy. ${farm.name} (${farm.crop})"
                snippet = "Area: ${farm.formattedArea()}"
                setOnMarkerClickListener { clickedMarker, _ ->
                    selectedFarm = farm
                    clickedMarker.showInfoWindow()
                    mapView.controller.animateTo(point, 16.0, 600L)

                    result.text = farm.displayInfo()
                    btnGoogleMapsArea.text = "GOOGLE MAPS: ${farm.name.uppercase()}"
                    btnGoogleMapsArea.visibility = View.VISIBLE
                    btnStreetView.visibility = View.VISIBLE
                    resultScroll.scrollTo(0, 0)

                    Toast.makeText(
                        this@MainActivity,
                        "Selected Brgy. ${farm.name} — tap button below to open in Google Maps",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            }

            mapView.overlays.add(marker)

            totalSqm += farm.squareMeters
            infoBuilder.append("${i + 1}. Brgy. ${farm.name} - ${farm.crop} (${farm.formattedArea()})")
            if (i < farms.size - 1) {
                infoBuilder.append("\n")
            }
        }

        mapView.zoomToBoundingBox(nasugbuBounds, true)
        mapView.invalidate()

        val formattedTotal = NumberFormat.getNumberInstance(Locale.US).format(totalSqm)
        result.text = "Mapped ${farms.size} Nasugbu Barangays\nTotal Area: $formattedTotal sq. m\n\n$infoBuilder"
        resultScroll.scrollTo(0, 0)
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