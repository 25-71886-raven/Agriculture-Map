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
import com.example.myapplication.ui.UserSession
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
    private lateinit var btnProfile: Button
    private lateinit var searchInput: EditText
    private lateinit var btnToggleLabels: Button
    private lateinit var btnStreetView: Button
    private lateinit var btnGoogleMapsArea: Button
    private lateinit var btnDirections: Button
    private lateinit var btnCloseInfo: Button
    private lateinit var infoPanel: LinearLayout
    private lateinit var infoLabel: TextView
    private lateinit var cropTapHintLabel: TextView
    private lateinit var cropGalleryScroll: HorizontalScrollView
    private lateinit var cropGalleryRow: LinearLayout
    private lateinit var result: TextView
    private lateinit var resultScroll: ScrollView
    private lateinit var subtitleText: TextView

    private lateinit var mainMapScreen: LinearLayout
    private lateinit var loginScreen: LinearLayout
    private lateinit var profileOverlay: LinearLayout
    private lateinit var cropPreviewOverlay: LinearLayout
    private lateinit var previewCropImage: ImageView
    private lateinit var previewCropTitle: TextView
    private lateinit var previewCropDesc: TextView
    private lateinit var btnSearchCropGoogle: Button
    private lateinit var bindProfileSession: (UserSession) -> Unit

    private lateinit var farms: MutableList<Farm>
    private var selectedFarm: Farm? = null
    private var tappedCustomPoint: GeoPoint? = null
    private var selectedCropForSearch: String = ""
    private var activeCropFilter: String = "ALL"
    private var activeSearchQuery: String = ""
    private var showMapLabels: Boolean = false
    private var isTopBarHidden: Boolean = false
    private var mapModeIndex: Int = 0
    private val filterButtons = mutableMapOf<String, Button>()
    private var splashDismissed = false
    private var currentUserSession: UserSession = UserSession(
        fullName = "Public Visitor",
        username = "guest",
        role = "Guest",
        isGuest = true
    )

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
                    Color.parseColor("#0B4F32")
                )
            )
        } else {
            @Suppress("DEPRECATION")
            setTaskDescription(
                ActivityManager.TaskDescription(
                    "Nasugbu Agricultural Map",
                    logoBitmap,
                    Color.parseColor("#0B4F32")
                )
            )
        }

        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = "NasugbuAgriculturalMap/2026.12 (Android; Batangas)"
        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache = File(osmConfig.osmdroidBasePath, "tiles")

        val rootContainer = FrameLayout(this)

        mainMapScreen = buildMainMapScreen()
        cropPreviewOverlay = buildCropPreviewOverlay()

        val profilePair = AuthScreens.buildProfileOverlay(
            context = this,
            onCloseProfile = {
                profileOverlay.visibility = View.GONE
            },
            onSignOut = {
                profileOverlay.visibility = View.GONE
                hideFarmInfoPanel()
                loginScreen.visibility = View.VISIBLE
            }
        )
        profileOverlay = profilePair.first
        bindProfileSession = profilePair.second

        loginScreen = AuthScreens.buildLoginScreen(this) { session, subtitleLabel ->
            currentUserSession = session
            subtitleText.text = subtitleLabel
            btnProfile.text = "PROFILE (${session.avatarInitials()})"
            bindProfileSession(session)
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
            cropPreviewOverlay,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )
        rootContainer.addView(
            profileOverlay,
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

    private fun openUserProfileModal() {
        bindProfileSession(currentUserSession)
        profileOverlay.visibility = View.VISIBLE
    }

    private fun buildCropPreviewOverlay(): LinearLayout {
        val density = resources.displayMetrics.density

        val backdrop = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(34, 34, 34, 34)
            setBackgroundColor(Color.argb(185, 10, 37, 64))
            visibility = View.GONE
            isClickable = true
            isFocusable = true
            setOnClickListener { visibility = View.GONE }
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            elevation = 14f * density
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 18f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isClickable = true
            isFocusable = true
        }

        val topStrip = View(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.parseColor("#137333"),
                    Color.parseColor("#1A73E8")
                )
            ).apply {
                cornerRadii = floatArrayOf(
                    18f * density, 18f * density,
                    18f * density, 18f * density,
                    0f, 0f,
                    0f, 0f
                )
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (6 * density).toInt()
            )
        }

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 24, 28, 24)
        }

        val badge = TextView(this).apply {
            text = "CROP VISUAL REFERENCE & GOOGLE SEARCH"
            textSize = 9.5f
            letterSpacing = 0.05f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#137333"))
            setPadding(16, 6, 16, 6)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E6F4EA"))
                cornerRadius = 50f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 14)
            }
        }

        previewCropImage = ImageView(this).apply {
            val imgWidth = (156 * density).toInt()
            val imgHeight = (126 * density).toInt()
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams(imgWidth, imgHeight).apply {
                setMargins(0, 0, 0, 14)
            }
        }

        previewCropTitle = TextView(this).apply {
            text = "Crop Name"
            textSize = 19f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#0F172A"))
            gravity = Gravity.CENTER
        }

        previewCropDesc = TextView(this).apply {
            text = ""
            textSize = 12.5f
            setTextColor(Color.parseColor("#475569"))
            gravity = Gravity.CENTER
            setLineSpacing(4f, 1.15f)
            setPadding(16, 12, 16, 12)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F8FAFC"))
                cornerRadius = 12f * density
                setStroke((1 * density).toInt(), Color.parseColor("#E2E8F0"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 16)
            }
        }

        btnSearchCropGoogle = Button(this).apply {
            text = "SEARCH ACTUAL PHOTOS ON GOOGLE"
            textSize = 11.5f
            letterSpacing = 0.03f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(20, 18, 20, 18)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A73E8"))
                cornerRadius = 12f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
            setOnClickListener {
                if (selectedCropForSearch.isNotEmpty()) {
                    searchCropOnGoogleImages(selectedCropForSearch)
                }
            }
        }

        val btnClosePreview = Button(this).apply {
            text = "CLOSE PREVIEW"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#334155"))
            setPadding(20, 14, 20, 14)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1F5F9"))
                cornerRadius = 12f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { backdrop.visibility = View.GONE }
        }

        body.addView(badge)
        body.addView(previewCropImage)
        body.addView(previewCropTitle)
        body.addView(previewCropDesc)
        body.addView(btnSearchCropGoogle)
        body.addView(btnClosePreview)

        card.addView(topStrip)
        card.addView(body)
        backdrop.addView(card)

        return backdrop
    }

    private fun openCropPreviewModal(cropName: String) {
        selectedCropForSearch = cropName
        previewCropImage.setImageBitmap(MapGraphics.createSingleCropCardBitmap(this, cropName))
        previewCropTitle.text = cropName
        previewCropDesc.text = getCropDescription(cropName)
        btnSearchCropGoogle.text = "SEARCH '${cropName.uppercase(Locale.US)}' ON GOOGLE"
        cropPreviewOverlay.visibility = View.VISIBLE
    }

    private fun getCropDescription(cropName: String): String {
        val c = cropName.lowercase(Locale.US)
        return when {
            c.contains("sugarcane") -> "Sugarcane (Tubo): Major commercial crop in Nasugbu harvested for raw sugar and molasses production."
            c.contains("rice") -> "Rice (Palay): Staple grain grown in irrigated and lowland paddies across Nasugbu barangays."
            c.contains("corn") -> "Corn (Mais): Cultivated for both human consumption and local agricultural supply."
            c.contains("coconut") -> "Coconut (Niyog): Versatile coastal and upland palm tree harvested for copra, buko, and coconut oil."
            c.contains("nipa") -> "Nipa Palm: Estuarine palm grown along riverbanks and coastal wetlands for thatch and sap."
            c.contains("dragon fruit") -> "Dragon Fruit (Pitaya): Climbing cactus fruit with bright magenta skin and sweet seed-speckled flesh."
            c.contains("mango") -> "Mango (Mangga): Sweet tropical stone fruit widely grown in Nasugbu orchards."
            c.contains("banana") -> "Banana (Saging): Year-round tropical fruit crop including Saba, Lakatan, and Latundan varieties."
            c.contains("pineapple") -> "Pineapple (Pinya): Upland tropical bromeliad fruit cultivated in elevated barangays like Aga and Kaylaway."
            c.contains("coffee") -> "Coffee (Kape): Upland agro-forestry crop grown in cool, elevated slopes of Nasugbu."
            c.contains("cacao") -> "Cacao: Shade-grown tropical tree whose pods produce beans for tablea and chocolate."
            c.contains("cassava") -> "Cassava (Kamoteng Kahoy): Drought-tolerant starchy root crop harvested for food and flour."
            c.contains("sweet potato") -> "Sweet Potato (Kamote): Nutritious creeping vine root crop grown in upland and lowland plots."
            c.contains("cashew") -> "Cashew (Kasuy): Coastal and hillside tree prized for its roasted nuts and juicy cashew apple."
            c.contains("watermelon") -> "Watermelon (Pakwan): Refreshing vining fruit grown in seasonal lowland plots such as Talangan."
            c.contains("avocado") -> "Avocado: Nutrient-rich orchard fruit tree thriving in Nasugbu's upland barangays."
            c.contains("jackfruit") -> "Jackfruit (Langka): Large tropical tree fruit used both ripe as dessert and unripe as vegetable."
            c.contains("sayote") -> "Sayote (Chayote): Climbing vine vegetable widely cultivated in cool upland farms like Brgy. Aga."
            else -> "$cropName: Cultivated agricultural crop in Nasugbu, Batangas. Tap below to view actual photos on Google."
        }
    }

    private fun searchCropOnGoogleImages(cropName: String) {
        try {
            val encodedQuery = Uri.encode("$cropName plant crop agriculture Philippines")
            val googleImageSearchUrl = "https://www.google.com/search?tbm=isch&q=$encodedQuery"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(googleImageSearchUrl)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No web browser app found on this device.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to search Google: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildMainMapScreen(): LinearLayout {
        val density = resources.displayMetrics.density

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 22)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#EEF4F0"),
                    Color.parseColor("#F4F7FA")
                )
            )
        }

        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 16, 18, 16)
            elevation = 8f * density
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.parseColor("#0B4F32"),
                    Color.parseColor("#137333"),
                    Color.parseColor("#0D5C75")
                )
            ).apply {
                cornerRadius = 26f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        val logoBadge = FrameLayout(this).apply {
            val badgeSize = (46 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(badgeSize, badgeSize).apply {
                setMargins(0, 0, 12, 0)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
                setStroke((2 * density).toInt(), Color.parseColor("#A8DAB5"))
            }
            setPadding(4, 4, 4, 4)
        }

        val headerLogo = ImageView(this).apply {
            setImageBitmap(MapGraphics.loadAppLogoBitmap(this@MainActivity))
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setOnClickListener {
                hideFarmInfoPanel()
                plotFarmsOnMap(autoZoomSingleMatch = false)
                mapView.controller.animateTo(NasugbuData.center, 12.6, 500L)
            }
        }
        logoBadge.addView(headerLogo)

        val titleColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { openUserProfileModal() }
        }

        val title = TextView(this).apply {
            text = "NASUGBU AGRI GIS"
            textSize = 15f
            letterSpacing = 0.04f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
        }

        subtitleText = TextView(this).apply {
            text = "Tap map or pin for GPS & crops"
            textSize = 10.5f
            setTextColor(Color.parseColor("#CEEAD6"))
            setTypeface(null, Typeface.BOLD)
        }

        titleColumn.addView(title)
        titleColumn.addView(subtitleText)

        btnToggleTopBar = Button(this).apply {
            text = "HIDE ▲"
            textSize = 9.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(16, 8, 16, 8)
            background = GradientDrawable().apply {
                setColor(Color.argb(45, 255, 255, 255))
                cornerRadius = 50f * density
                setStroke((1 * density).toInt(), Color.argb(95, 255, 255, 255))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(6, 0, 0, 0)
            }
            setOnClickListener { toggleTopControlsBar() }
        }

        btnProfile = Button(this).apply {
            text = "PROFILE"
            textSize = 9.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#0B4F32"))
            setPadding(18, 8, 18, 8)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 50f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(6, 0, 0, 0)
            }
            setOnClickListener { openUserProfileModal() }
        }

        headerRow.addView(logoBadge)
        headerRow.addView(titleColumn)
        headerRow.addView(btnToggleTopBar)
        headerRow.addView(btnProfile)

        topControlsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 6, 10, 6)
            elevation = 4f * density
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 50f * density
                setStroke((1.5f * density).toInt(), Color.parseColor("#D2E3FC"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        }

        searchInput = EditText(this).apply {
            hint = "Search barangay or crop (e.g. Tumalim, Dragon Fruit)..."
            textSize = 13f
            setSingleLine(true)
            setPadding(20, 12, 16, 12)
            background = null
            setTextColor(Color.parseColor("#202124"))
            setHintTextColor(Color.parseColor("#80868B"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnClearSearch = Button(this).apply {
            text = "CLEAR"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#3C4043"))
            setPadding(18, 8, 18, 8)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1F3F4"))
                cornerRadius = 50f * density
            }
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
                setMargins(0, 0, 0, 10)
            }
        }

        val filterRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(2, 2, 2, 4)
        }

        val categories = listOf(
            "ALL" to "ALL (${farms.size})",
            "SUGARCANE" to "SUGARCANE",
            "RICE" to "RICE",
            "COCONUT" to "COCONUT",
            "DRAGON_FRUIT" to "DRAGON FRUIT",
            "FRUIT" to "MANGO / FRUITS",
            "VEG" to "CORN / VEG"
        )

        for ((key, label) in categories) {
            val chipBtn = Button(this).apply {
                text = label
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setPadding(24, 10, 24, 10)
                elevation = 2f * density
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
                setMargins(0, 0, 0, 10)
            }
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(2, 2, 2, 4)
        }

        val btnDisplay = Button(this).apply {
            text = "CENTER MAP"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(22, 10, 22, 10)
            elevation = 3f * density
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A73E8"))
                cornerRadius = 50f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener {
                hideFarmInfoPanel()
                mapView.controller.animateTo(NasugbuData.center, 12.6, 500L)
            }
        }

        btnToggleLabels = Button(this).apply {
            text = "SHOW BRGY CARDS"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#202124"))
            setPadding(22, 10, 22, 10)
            elevation = 3f * density
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 50f * density
                setStroke((1.5f * density).toInt(), Color.parseColor("#DADCE0"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener {
                showMapLabels = !showMapLabels
                text = if (showMapLabels) "HIDE BRGY CARDS" else "SHOW BRGY CARDS"
                setTextColor(if (showMapLabels) Color.WHITE else Color.parseColor("#202124"))
                background = GradientDrawable().apply {
                    setColor(if (showMapLabels) Color.parseColor("#E37400") else Color.WHITE)
                    cornerRadius = 50f * density
                    if (!showMapLabels) {
                        setStroke((1.5f * density).toInt(), Color.parseColor("#DADCE0"))
                    }
                }
                plotFarmsOnMap(autoZoomSingleMatch = false, highlightedFarmName = selectedFarm?.name)
            }
        }

        val btnLayer = Button(this).apply {
            text = "SATELLITE 3D"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(22, 10, 22, 10)
            elevation = 3f * density
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#137333"))
                cornerRadius = 50f * density
            }
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
                        background = GradientDrawable().apply {
                            setColor(Color.parseColor("#137333"))
                            cornerRadius = 50f * density
                        }
                    }
                    1 -> {
                        mapView.setTileSource(MapTileSources.satelliteHybrid2026)
                        mapView.controller.setZoom(14.5)
                        text = "TERRAIN 2026"
                        background = GradientDrawable().apply {
                            setColor(Color.parseColor("#0B57D0"))
                            cornerRadius = 50f * density
                        }
                        Toast.makeText(
                            this@MainActivity,
                            "Hybrid Satellite View Active (2026 Imagery)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        mapView.setTileSource(MapTileSources.terrain2026)
                        text = "ROAD MAP"
                        background = GradientDrawable().apply {
                            setColor(Color.parseColor("#E37400"))
                            cornerRadius = 50f * density
                        }
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
            elevation = 6f * density
            setPadding(4, 4, 4, 4)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 24f * density
                setStroke((2 * density).toInt(), Color.parseColor("#CEEAD6"))
            }
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
            elevation = 10f * density
            setPadding(24, 14, 24, 20)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 28f * density
                setStroke((2 * density).toInt(), Color.parseColor("#CEEAD6"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 12, 0, 0)
            }
        }

        val dragPill = View(this).apply {
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#DADCE0"))
                cornerRadius = 50f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                (44 * density).toInt(),
                (4 * density).toInt()
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, 10)
            }
        }

        val infoHeaderRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        infoLabel = TextView(this).apply {
            text = "BARANGAY AGRICULTURAL DETAILS"
            textSize = 12f
            letterSpacing = 0.03f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#137333"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        btnCloseInfo = Button(this).apply {
            text = "CLOSE ✕"
            textSize = 9.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#5F6368"))
            setPadding(18, 6, 18, 6)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1F3F4"))
                cornerRadius = 50f * density
            }
            setOnClickListener { hideFarmInfoPanel() }
        }

        infoHeaderRow.addView(infoLabel)
        infoHeaderRow.addView(btnCloseInfo)

        cropTapHintLabel = TextView(this).apply {
            text = "Tap any crop card below to view details & search photos on Google:"
            textSize = 10.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1A73E8"))
            visibility = View.GONE
            setPadding(0, 6, 0, 0)
        }

        cropGalleryScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 6, 0, 8)
            }
        }

        cropGalleryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        cropGalleryScroll.addView(cropGalleryRow)

        resultScroll = ScrollView(this).apply {
            setPadding(16, 12, 16, 12)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F8FAFC"))
                cornerRadius = 18f * density
                setStroke((1 * density).toInt(), Color.parseColor("#E8EAED"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 12)
            }
        }

        result = TextView(this).apply {
            textSize = 12.5f
            setTextColor(Color.parseColor("#202124"))
            setLineSpacing(5f, 1.18f)
        }
        resultScroll.addView(result)

        val actionButtonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        btnGoogleMapsArea = Button(this).apply {
            text = "MAPS"
            textSize = 10.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(12, 12, 12, 12)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A73E8"))
                cornerRadius = 50f * density
            }
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
            setTextColor(Color.WHITE)
            setPadding(12, 12, 12, 12)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#137333"))
                cornerRadius = 50f * density
            }
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
            setTextColor(Color.WHITE)
            setPadding(12, 12, 12, 12)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E37400"))
                cornerRadius = 50f * density
            }
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

        infoPanel.addView(dragPill)
        infoPanel.addView(infoHeaderRow)
        infoPanel.addView(cropTapHintLabel)
        infoPanel.addView(cropGalleryScroll)
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

    private fun renderCropPicturesForFarm(farm: Farm) {
        cropGalleryRow.removeAllViews()
        val cropList = MapGraphics.parseBarangayCropItems(farm.crop)
        if (cropList.isEmpty()) {
            cropTapHintLabel.visibility = View.GONE
            cropGalleryScroll.visibility = View.GONE
            return
        }
        val gapPx = (8 * resources.displayMetrics.density).toInt()
        for (cropName in cropList) {
            val cropImg = ImageView(this).apply {
                setImageBitmap(MapGraphics.createSingleCropCardBitmap(this@MainActivity, cropName))
                adjustViewBounds = true
                isClickable = true
                isFocusable = true
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, gapPx, 0)
                }
                setOnClickListener {
                    openCropPreviewModal(cropName)
                }
            }
            cropGalleryRow.addView(cropImg)
        }
        cropGalleryScroll.scrollTo(0, 0)
        cropTapHintLabel.visibility = View.VISIBLE
        cropGalleryScroll.visibility = View.VISIBLE
    }

    private fun getActiveTargetCoordinates(): Pair<Double, Double>? {
        selectedFarm?.let { return Pair(it.lat, it.lng) }
        tappedCustomPoint?.let { return Pair(it.latitude, it.longitude) }
        return null
    }

    private fun toggleTopControlsBar() {
        val density = resources.displayMetrics.density
        isTopBarHidden = !isTopBarHidden
        if (isTopBarHidden) {
            topControlsContainer.visibility = View.GONE
            btnToggleTopBar.text = "SHOW ▼"
            btnToggleTopBar.setTextColor(Color.parseColor("#0B4F32"))
            btnToggleTopBar.background = GradientDrawable().apply {
                setColor(Color.parseColor("#CEEAD6"))
                cornerRadius = 50f * density
            }
        } else {
            topControlsContainer.visibility = View.VISIBLE
            btnToggleTopBar.text = "HIDE ▲"
            btnToggleTopBar.setTextColor(Color.WHITE)
            btnToggleTopBar.background = GradientDrawable().apply {
                setColor(Color.argb(45, 255, 255, 255))
                cornerRadius = 50f * density
                setStroke((1 * density).toInt(), Color.argb(95, 255, 255, 255))
            }
        }
        mapView.invalidate()
    }

    private fun updateFilterChipStyles() {
        val density = resources.displayMetrics.density
        for ((key, btn) in filterButtons) {
            val isSelected = key == activeCropFilter
            val activeBg = if (key == "DRAGON_FRUIT") Color.parseColor("#D81B60") else Color.parseColor("#137333")
            btn.background = GradientDrawable().apply {
                setColor(if (isSelected) activeBg else Color.WHITE)
                cornerRadius = 50f * density
                if (!isSelected) {
                    setStroke((1.5f * density).toInt(), Color.parseColor("#DADCE0"))
                }
            }
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
        renderCropPicturesForFarm(farm)
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
        cropTapHintLabel.visibility = View.GONE
        cropGalleryScroll.visibility = View.GONE
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
        cropTapHintLabel.visibility = View.GONE
        cropGalleryScroll.visibility = View.GONE
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
            Toast.makeText(this, "No Google Maps or Browser app installed on this device.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open map: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCoordinatesDirections(lat: Double, lng: Double) {
        try {
            val directionsUrl = "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(directionsUrl)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No Google Maps or Browser app installed for directions.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open directions: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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