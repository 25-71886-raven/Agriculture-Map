package com.example.myapplication.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.map.MapGraphics
import java.util.Locale

data class UserSession(
    val fullName: String,
    val username: String,
    val role: String,
    val isGuest: Boolean
) {
    fun displayBadge(): String {
        return if (isGuest) "Guest Observer" else "$role — $fullName"
    }

    fun avatarInitials(): String {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase(Locale.US)
            parts.size == 1 -> parts[0].take(2).uppercase(Locale.US)
            else -> "US"
        }
    }
}

object AuthScreens {

    private const val PREFS_NAME = "NasugbuAgriUsersPrefs"

    fun buildSplashScreen(
        context: Context,
        onContinueClicked: () -> Unit
    ): LinearLayout {
        val density = context.resources.displayMetrics.density

        val splashLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#0A2540"),
                    Color.parseColor("#0E3B2E")
                )
            )
            isClickable = true
            isFocusable = true
        }

        val republicHeader = TextView(context).apply {
            text = "REPUBLIC OF THE PHILIPPINES • PROVINCE OF BATANGAS"
            textSize = 10f
            letterSpacing = 0.08f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#D4AF37"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 18)
        }

        val sealContainer = FrameLayout(context).apply {
            val sealSize = (138 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(sealSize, sealSize).apply {
                setMargins(0, 0, 0, 22)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
                setStroke((3 * density).toInt(), Color.parseColor("#D4AF37"))
            }
            setPadding(8, 8, 8, 8)
        }

        val logoView = ImageView(context).apply {
            setImageBitmap(MapGraphics.loadAppLogoBitmap(context))
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(600L)
                .start()
        }
        sealContainer.addView(logoView)

        val appTitle = TextView(context).apply {
            text = "MUNICIPALITY OF NASUGBU\nAGRICULTURAL GIS PORTAL"
            textSize = 20f
            letterSpacing = 0.04f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setLineSpacing(4f, 1.15f)
        }

        val dividerLine = View(context).apply {
            setBackgroundColor(Color.parseColor("#D4AF37"))
            layoutParams = LinearLayout.LayoutParams(
                (90 * density).toInt(),
                (2 * density).toInt()
            ).apply {
                setMargins(0, 14, 0, 14)
            }
        }

        val appSubtitle = TextView(context).apply {
            text = "Office of the Municipal Agriculturist\nCrop & Barangay Land Area Information System (2026)"
            textSize = 12.5f
            setTextColor(Color.parseColor("#CBD5E1"))
            gravity = Gravity.CENTER
            setLineSpacing(3f, 1.15f)
            setPadding(0, 0, 0, 28)
        }

        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 18)
            }
        }

        val btnContinue = Button(context).apply {
            text = "ACCESS PORTAL"
            textSize = 11.5f
            letterSpacing = 0.06f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#0A2540"))
            setPadding(48, 18, 48, 18)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 8f * density
            }
            setOnClickListener { onContinueClicked() }
        }

        splashLayout.addView(republicHeader)
        splashLayout.addView(sealContainer)
        splashLayout.addView(appTitle)
        splashLayout.addView(dividerLine)
        splashLayout.addView(appSubtitle)
        splashLayout.addView(progressBar)
        splashLayout.addView(btnContinue)

        return splashLayout
    }

    fun buildLoginScreen(
        context: Context,
        onLoginSuccess: (session: UserSession, subtitleLabel: String) -> Unit
    ): LinearLayout {
        val density = context.resources.displayMetrics.density
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var isRegisterMode = false
        var selectedRole = "Local Farmer"

        val outerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.parseColor("#0A2540"),
                    Color.parseColor("#102A43"),
                    Color.parseColor("#0E3B2E")
                )
            )
            isClickable = true
            isFocusable = true
        }

        val scrollView = ScrollView(context).apply {
            isFillViewport = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val scrollContent = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            elevation = 12f * density
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 12f * density
                setStroke((1 * density).toInt(), Color.parseColor("#CBD5E1"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Formal Top Institutional Accent Bar
        val topAccentStrip = View(context).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.parseColor("#0A2540"),
                    Color.parseColor("#1B5E20"),
                    Color.parseColor("#C59B27")
                )
            ).apply {
                cornerRadii = floatArrayOf(
                    12f * density, 12f * density,
                    12f * density, 12f * density,
                    0f, 0f,
                    0f, 0f
                )
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (6 * density).toInt()
            )
        }

        val cardBody = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 28, 32, 28)
        }

        val govHeaderRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 14)
            }
        }

        val logoFrame = FrameLayout(context).apply {
            val sizePx = (56 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                setMargins(0, 0, 14, 0)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#F8FAFC"))
                setStroke((1.5f * density).toInt(), Color.parseColor("#CBD5E1"))
            }
            setPadding(4, 4, 4, 4)
        }

        val logoView = ImageView(context).apply {
            setImageBitmap(MapGraphics.loadAppLogoBitmap(context))
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        logoFrame.addView(logoView)

        val govTitleCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val deptEyebrow = TextView(context).apply {
            text = "MUNICIPALITY OF NASUGBU • BATANGAS"
            textSize = 9.5f
            letterSpacing = 0.06f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1B5E20"))
        }

        val title = TextView(context).apply {
            text = "AUTHORIZED PORTAL SIGN IN"
            textSize = 15.5f
            letterSpacing = 0.02f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#0F172A"))
            setPadding(0, 2, 0, 2)
        }

        val subtitle = TextView(context).apply {
            text = "Agricultural Land & Crop GIS Registry (2026)"
            textSize = 11f
            setTextColor(Color.parseColor("#475569"))
        }

        govTitleCol.addView(deptEyebrow)
        govTitleCol.addView(title)
        govTitleCol.addView(subtitle)

        govHeaderRow.addView(logoFrame)
        govHeaderRow.addView(govTitleCol)

        val headerDivider = View(context).apply {
            setBackgroundColor(Color.parseColor("#E2E8F0"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        // Formal Mode Selector Tabs
        val tabRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(4, 4, 4, 4)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1F5F9"))
                cornerRadius = 8f * density
                setStroke((1 * density).toInt(), Color.parseColor("#CBD5E1"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 18)
            }
        }

        val tabSignIn = Button(context).apply {
            text = "SIGN IN"
            textSize = 10.5f
            letterSpacing = 0.05f
            setTypeface(null, Typeface.BOLD)
            setPadding(12, 12, 12, 12)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 4, 0)
            }
        }

        val tabCreateAcc = Button(context).apply {
            text = "REGISTER ACCOUNT"
            textSize = 10.5f
            letterSpacing = 0.05f
            setTypeface(null, Typeface.BOLD)
            setPadding(12, 12, 12, 12)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 0, 0, 0)
            }
        }

        tabRow.addView(tabSignIn)
        tabRow.addView(tabCreateAcc)

        fun createFieldLabel(labelText: String): TextView {
            return TextView(context).apply {
                text = labelText
                textSize = 10f
                letterSpacing = 0.05f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#334155"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(2, 0, 0, 5)
                }
            }
        }

        fun createFormalInput(hintText: String, isPassword: Boolean = false): EditText {
            return EditText(context).apply {
                hint = hintText
                textSize = 13f
                setSingleLine(true)
                if (isPassword) {
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                setPadding(22, 18, 22, 18)
                setTextColor(Color.parseColor("#0F172A"))
                setHintTextColor(Color.parseColor("#94A3B8"))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F8FAFC"))
                    cornerRadius = 8f * density
                    setStroke((1.2f * density).toInt(), Color.parseColor("#CBD5E1"))
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 14)
                }
            }
        }

        val fullNameLabel = createFieldLabel("OFFICIAL FULL NAME").apply { visibility = View.GONE }
        val fullNameInput = createFormalInput("e.g., Juan Dela Cruz").apply { visibility = View.GONE }

        val usernameLabel = createFieldLabel("USERNAME OR OFFICIAL EMAIL")
        val usernameInput = createFormalInput("Enter username or email")

        val passwordLabel = createFieldLabel("PASSWORD")
        val passwordInput = createFormalInput("Enter password", isPassword = true)

        val confirmPasswordLabel = createFieldLabel("CONFIRM PASSWORD").apply { visibility = View.GONE }
        val confirmPasswordInput = createFormalInput("Re-enter password", isPassword = true).apply {
            visibility = View.GONE
        }

        val roleContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 14)
            }
        }

        val roleTitle = createFieldLabel("DESIGNATED STAKEHOLDER ROLE")

        val roleButtonsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val roles = listOf("Local Farmer", "Agri Planner", "Researcher")
        val roleButtons = mutableMapOf<String, Button>()

        fun updateRoleButtonStyles() {
            for ((rName, btn) in roleButtons) {
                val active = rName == selectedRole
                btn.background = GradientDrawable().apply {
                    setColor(if (active) Color.parseColor("#0A2540") else Color.parseColor("#F1F5F9"))
                    cornerRadius = 6f * density
                    setStroke(
                        (1 * density).toInt(),
                        if (active) Color.parseColor("#0A2540") else Color.parseColor("#CBD5E1")
                    )
                }
                btn.setTextColor(if (active) Color.WHITE else Color.parseColor("#334155"))
            }
        }

        for (r in roles) {
            val rBtn = Button(context).apply {
                text = r
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setPadding(8, 12, 8, 12)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(3, 0, 3, 0)
                }
                setOnClickListener {
                    selectedRole = r
                    updateRoleButtonStyles()
                }
            }
            roleButtons[r] = rBtn
            roleButtonsRow.addView(rBtn)
        }
        updateRoleButtonStyles()

        roleContainer.addView(roleTitle)
        roleContainer.addView(roleButtonsRow)

        val errorLabel = TextView(context).apply {
            text = ""
            textSize = 11.5f
            setTextColor(Color.parseColor("#B91C1C"))
            setPadding(16, 12, 16, 12)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FEF2F2"))
                cornerRadius = 6f * density
                setStroke((1 * density).toInt(), Color.parseColor("#FECACA"))
            }
            visibility = View.GONE
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 14)
            }
        }

        val btnPrimaryAction = Button(context).apply {
            text = "PROCEED TO SIGN IN"
            textSize = 12f
            letterSpacing = 0.05f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(24, 20, 24, 20)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0A2540"))
                cornerRadius = 8f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 10)
            }
        }

        val btnGuest = Button(context).apply {
            text = "PUBLIC GUEST ACCESS (READ-ONLY)"
            textSize = 10.5f
            letterSpacing = 0.04f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1B5E20"))
            setPadding(16, 16, 16, 16)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F0FDF4"))
                cornerRadius = 8f * density
                setStroke((1.2f * density).toInt(), Color.parseColor("#BBF7D0"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            setOnClickListener {
                errorLabel.visibility = View.GONE
                val guestSession = UserSession(
                    fullName = "Public Observer",
                    username = "guest",
                    role = "Guest Observer",
                    isGuest = true
                )
                onLoginSuccess(
                    guestSession,
                    "Session: Guest Observer • Tap PROFILE for account details"
                )
            }
        }

        val btnToggleAuthPrompt = TextView(context).apply {
            text = "New personnel or farmer? Register an official account"
            textSize = 11.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#0A2540"))
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val credentialsBox = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F8FAFC"))
                cornerRadius = 6f * density
                setStroke((1 * density).toInt(), Color.parseColor("#E2E8F0"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 12, 0, 0)
            }
        }

        val credentialsHeader = TextView(context).apply {
            text = "OFFICIAL TEST CREDENTIALS"
            textSize = 9.5f
            letterSpacing = 0.05f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#475569"))
            gravity = Gravity.CENTER
        }

        val hintNote = TextView(context).apply {
            text = "Admin: admin / admin123   •   Farmer: farmer / nasugbu2026"
            textSize = 10.5f
            setTextColor(Color.parseColor("#64748B"))
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 0)
        }

        credentialsBox.addView(credentialsHeader)
        credentialsBox.addView(hintNote)

        fun syncAuthModeUI() {
            errorLabel.visibility = View.GONE
            if (isRegisterMode) {
                title.text = "ACCOUNT REGISTRATION"
                subtitle.text = "Enroll a New Agricultural GIS Stakeholder Profile"
                fullNameLabel.visibility = View.VISIBLE
                fullNameInput.visibility = View.VISIBLE
                confirmPasswordLabel.visibility = View.VISIBLE
                confirmPasswordInput.visibility = View.VISIBLE
                roleContainer.visibility = View.VISIBLE
                btnPrimaryAction.text = "SUBMIT REGISTRATION & SIGN IN"
                btnToggleAuthPrompt.text = "Already registered? Return to Sign In"
                credentialsBox.visibility = View.GONE

                tabCreateAcc.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#0A2540"))
                    cornerRadius = 6f * density
                }
                tabCreateAcc.setTextColor(Color.WHITE)

                tabSignIn.background = null
                tabSignIn.setTextColor(Color.parseColor("#475569"))
            } else {
                title.text = "AUTHORIZED PORTAL SIGN IN"
                subtitle.text = "Agricultural Land & Crop GIS Registry (2026)"
                fullNameLabel.visibility = View.GONE
                fullNameInput.visibility = View.GONE
                confirmPasswordLabel.visibility = View.GONE
                confirmPasswordInput.visibility = View.GONE
                roleContainer.visibility = View.GONE
                btnPrimaryAction.text = "PROCEED TO SIGN IN"
                btnToggleAuthPrompt.text = "New personnel or farmer? Register an official account"
                credentialsBox.visibility = View.VISIBLE

                tabSignIn.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#0A2540"))
                    cornerRadius = 6f * density
                }
                tabSignIn.setTextColor(Color.WHITE)

                tabCreateAcc.background = null
                tabCreateAcc.setTextColor(Color.parseColor("#475569"))
            }
        }

        tabSignIn.setOnClickListener {
            isRegisterMode = false
            syncAuthModeUI()
        }

        tabCreateAcc.setOnClickListener {
            isRegisterMode = true
            syncAuthModeUI()
        }

        btnToggleAuthPrompt.setOnClickListener {
            isRegisterMode = !isRegisterMode
            syncAuthModeUI()
        }

        btnPrimaryAction.setOnClickListener {
            val userRaw = usernameInput.text.toString().trim()
            val userKey = userRaw.lowercase(Locale.US)
            val pass = passwordInput.text.toString().trim()

            if (isRegisterMode) {
                val fullName = fullNameInput.text.toString().trim()
                val confirmPass = confirmPasswordInput.text.toString().trim()

                when {
                    fullName.isEmpty() || userRaw.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() -> {
                        errorLabel.text = "Please complete all required registration fields."
                        errorLabel.visibility = View.VISIBLE
                    }
                    userKey == "admin" || userKey == "farmer" || prefs.contains("pwd_$userKey") -> {
                        errorLabel.text = "The username '$userRaw' is already registered in the system."
                        errorLabel.visibility = View.VISIBLE
                    }
                    pass.length < 4 -> {
                        errorLabel.text = "Security Policy: Password must contain at least 4 characters."
                        errorLabel.visibility = View.VISIBLE
                    }
                    pass != confirmPass -> {
                        errorLabel.text = "Password confirmation does not match."
                        errorLabel.visibility = View.VISIBLE
                    }
                    else -> {
                        errorLabel.visibility = View.GONE
                        prefs.edit()
                            .putString("pwd_$userKey", pass)
                            .putString("name_$userKey", fullName)
                            .putString("role_$userKey", selectedRole)
                            .apply()

                        val session = UserSession(
                            fullName = fullName,
                            username = userRaw,
                            role = selectedRole,
                            isGuest = false
                        )
                        fullNameInput.setText("")
                        usernameInput.setText("")
                        passwordInput.setText("")
                        confirmPasswordInput.setText("")
                        isRegisterMode = false
                        syncAuthModeUI()

                        Toast.makeText(context, "Registration verified. Welcome, ${session.fullName}.", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(session, "Active User: ${session.displayBadge()}")
                    }
                }
            } else {
                when {
                    userRaw.isEmpty() || pass.isEmpty() -> {
                        errorLabel.text = "Please provide both your username and password."
                        errorLabel.visibility = View.VISIBLE
                    }
                    userKey == "admin" && pass == "admin123" -> {
                        errorLabel.visibility = View.GONE
                        val session = UserSession(
                            fullName = "Municipal Agri Officer",
                            username = "admin",
                            role = "Municipal Agri Planner (Admin)",
                            isGuest = false
                        )
                        usernameInput.setText("")
                        passwordInput.setText("")
                        Toast.makeText(context, "Authenticated as ${session.displayBadge()}", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(session, "Active User: ${session.displayBadge()}")
                    }
                    userKey == "farmer" && pass == "nasugbu2026" -> {
                        errorLabel.visibility = View.GONE
                        val session = UserSession(
                            fullName = "Nasugbu Farmer",
                            username = "farmer",
                            role = "Local Farmer",
                            isGuest = false
                        )
                        usernameInput.setText("")
                        passwordInput.setText("")
                        Toast.makeText(context, "Authenticated as ${session.displayBadge()}", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(session, "Active User: ${session.displayBadge()}")
                    }
                    prefs.contains("pwd_$userKey") -> {
                        val savedPass = prefs.getString("pwd_$userKey", "")
                        if (savedPass == pass) {
                            errorLabel.visibility = View.GONE
                            val savedName = prefs.getString("name_$userKey", userRaw) ?: userRaw
                            val savedRole = prefs.getString("role_$userKey", "Registered User") ?: "Registered User"
                            val session = UserSession(
                                fullName = savedName,
                                username = userRaw,
                                role = savedRole,
                                isGuest = false
                            )
                            usernameInput.setText("")
                            passwordInput.setText("")
                            Toast.makeText(context, "Authenticated as ${session.displayBadge()}", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(session, "Active User: ${session.displayBadge()}")
                        } else {
                            errorLabel.text = "Authentication failed: Invalid password for '$userRaw'."
                            errorLabel.visibility = View.VISIBLE
                        }
                    }
                    else -> {
                        errorLabel.text = "Account record not found. Select 'Register Account' to enroll."
                        errorLabel.visibility = View.VISIBLE
                    }
                }
            }
        }

        syncAuthModeUI()

        cardBody.addView(govHeaderRow)
        cardBody.addView(headerDivider)
        cardBody.addView(tabRow)
        cardBody.addView(fullNameLabel)
        cardBody.addView(fullNameInput)
        cardBody.addView(usernameLabel)
        cardBody.addView(usernameInput)
        cardBody.addView(passwordLabel)
        cardBody.addView(passwordInput)
        cardBody.addView(confirmPasswordLabel)
        cardBody.addView(confirmPasswordInput)
        cardBody.addView(roleContainer)
        cardBody.addView(errorLabel)
        cardBody.addView(btnPrimaryAction)
        cardBody.addView(btnGuest)
        cardBody.addView(btnToggleAuthPrompt)
        cardBody.addView(credentialsBox)

        card.addView(topAccentStrip)
        card.addView(cardBody)

        scrollContent.addView(card)
        scrollView.addView(scrollContent)
        outerLayout.addView(scrollView)
        return outerLayout
    }

    fun buildProfileOverlay(
        context: Context,
        onCloseProfile: () -> Unit,
        onSignOut: () -> Unit
    ): Pair<LinearLayout, (UserSession) -> Unit> {
        val density = context.resources.displayMetrics.density

        val backdrop = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.argb(185, 10, 37, 64))
            visibility = View.GONE
            isClickable = true
            isFocusable = true
            setOnClickListener { onCloseProfile() }
        }

        val profileCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            elevation = 12f * density
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 12f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isClickable = true
            isFocusable = true
        }

        val topStrip = View(context).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.parseColor("#0A2540"),
                    Color.parseColor("#1B5E20"),
                    Color.parseColor("#C59B27")
                )
            ).apply {
                cornerRadii = floatArrayOf(
                    12f * density, 12f * density,
                    12f * density, 12f * density,
                    0f, 0f,
                    0f, 0f
                )
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (6 * density).toInt()
            )
        }

        val bodyCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(30, 26, 30, 26)
        }

        val headerBadge = TextView(context).apply {
            text = "MUNICIPAL GIS USER CREDENTIALS"
            textSize = 10f
            letterSpacing = 0.06f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#0A2540"))
            setPadding(18, 8, 18, 8)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1F5F9"))
                cornerRadius = 6f * density
                setStroke((1 * density).toInt(), Color.parseColor("#CBD5E1"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val avatarCircle = TextView(context).apply {
            text = "GU"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            val circlePx = (70 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(circlePx, circlePx).apply {
                setMargins(0, 0, 0, 12)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#0A2540"))
            }
        }

        val nameText = TextView(context).apply {
            text = "Public Observer"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#0F172A"))
            gravity = Gravity.CENTER
        }

        val usernameText = TextView(context).apply {
            text = "Account ID: @guest"
            textSize = 12f
            setTextColor(Color.parseColor("#64748B"))
            gravity = Gravity.CENTER
            setPadding(0, 2, 0, 16)
        }

        val detailsBox = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F8FAFC"))
                cornerRadius = 8f * density
                setStroke((1 * density).toInt(), Color.parseColor("#CBD5E1"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 18)
            }
        }

        val roleInfoText = TextView(context).apply {
            text = "Designation: Guest Observer"
            textSize = 12.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#0F172A"))
            setPadding(0, 0, 0, 6)
        }

        val accountTypeText = TextView(context).apply {
            text = "Clearance Level: Public Read-Only Access"
            textSize = 12f
            setTextColor(Color.parseColor("#334155"))
            setPadding(0, 0, 0, 6)
        }

        val jurisdictionText = TextView(context).apply {
            text = "Jurisdiction: Municipality of Nasugbu, Batangas"
            textSize = 12f
            setTextColor(Color.parseColor("#1B5E20"))
        }

        detailsBox.addView(roleInfoText)
        detailsBox.addView(accountTypeText)
        detailsBox.addView(jurisdictionText)

        val btnClose = Button(context).apply {
            text = "RETURN TO GIS MAP"
            textSize = 11.5f
            letterSpacing = 0.04f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(20, 16, 20, 16)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0A2540"))
                cornerRadius = 8f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
            setOnClickListener { onCloseProfile() }
        }

        val btnSignOut = Button(context).apply {
            text = "SIGN OUT / SWITCH USER"
            textSize = 11.5f
            letterSpacing = 0.04f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(20, 16, 20, 16)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#991B1B"))
                cornerRadius = 8f * density
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { onSignOut() }
        }

        bodyCol.addView(headerBadge)
        bodyCol.addView(avatarCircle)
        bodyCol.addView(nameText)
        bodyCol.addView(usernameText)
        bodyCol.addView(detailsBox)
        bodyCol.addView(btnClose)
        bodyCol.addView(btnSignOut)

        profileCard.addView(topStrip)
        profileCard.addView(bodyCol)

        backdrop.addView(profileCard)

        val bindSession: (UserSession) -> Unit = { session ->
            avatarCircle.text = session.avatarInitials()
            avatarCircle.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(if (session.isGuest) Color.parseColor("#334155") else Color.parseColor("#0A2540"))
            }
            nameText.text = session.fullName
            usernameText.text = "Account ID: @${session.username}"
            roleInfoText.text = "Designation: ${session.role}"
            accountTypeText.text = if (session.isGuest) {
                "Clearance Level: Public Read-Only Access"
            } else {
                "Clearance Level: Verified Registered Personnel"
            }
            btnSignOut.text = if (session.isGuest) "SIGN IN WITH ACCOUNT" else "SIGN OUT / SWITCH USER"
        }

        return Pair(backdrop, bindSession)
    }
}