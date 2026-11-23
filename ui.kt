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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.map.MapGraphics
import java.util.Locale

object AuthScreens {

    private const val PREFS_NAME = "NasugbuAgriUsersPrefs"

    fun buildSplashScreen(
        context: Context,
        onContinueClicked: () -> Unit
    ): LinearLayout {
        val splashLayout = LinearLayout(context).apply {
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

        val logoView = ImageView(context).apply {
            setImageBitmap(MapGraphics.loadAppLogoBitmap(context))
            scaleType = ImageView.ScaleType.FIT_CENTER
            val sizePx = (150 * context.resources.displayMetrics.density).toInt()
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

        val appTitle = TextView(context).apply {
            text = "NASUGBU AGRICULTURAL MAP"
            textSize = 23f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            letterSpacing = 0.04f
        }

        val appSubtitle = TextView(context).apply {
            text = "Municipal Crop & Barangay Land Area Map\nNasugbu, Batangas (2026 GIS Edition)"
            textSize = 14f
            setTextColor(Color.parseColor("#E8F5E9"))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }

        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val btnContinue = Button(context).apply {
            text = "PROCEED TO SIGN IN"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#137333"))
            setPadding(44, 18, 44, 18)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 50f
            }
            setOnClickListener { onContinueClicked() }
        }

        splashLayout.addView(logoView)
        splashLayout.addView(appTitle)
        splashLayout.addView(appSubtitle)
        splashLayout.addView(progressBar)
        splashLayout.addView(btnContinue)

        return splashLayout
    }

    fun buildLoginScreen(
        context: Context,
        onLoginSuccess: (role: String, subtitleLabel: String) -> Unit
    ): LinearLayout {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var isRegisterMode = false
        var selectedRole = "Local Farmer"

        val outerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
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
            setPadding(34, 36, 34, 36)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 32f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val logoView = ImageView(context).apply {
            setImageBitmap(MapGraphics.loadAppLogoBitmap(context))
            scaleType = ImageView.ScaleType.FIT_CENTER
            val sizePx = (78 * context.resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        val title = TextView(context).apply {
            text = "SIGN IN"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#137333"))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(context).apply {
            text = "Nasugbu Municipal Agricultural GIS System (2026)"
            textSize = 11.5f
            setTextColor(Color.parseColor("#5F6368"))
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 16)
        }

        // Mode Switcher Tabs: SIGN IN | CREATE ACCOUNT
        val tabRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(6, 6, 6, 6)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F1F3F4"))
                cornerRadius = 20f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val tabSignIn = Button(context).apply {
            text = "SIGN IN"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setPadding(12, 12, 12, 12)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 4, 0)
            }
        }

        val tabCreateAcc = Button(context).apply {
            text = "CREATE ACCOUNT"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setPadding(12, 12, 12, 12)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(4, 0, 0, 0)
            }
        }

        tabRow.addView(tabSignIn)
        tabRow.addView(tabCreateAcc)

        fun createStyledInput(hintText: String, isPassword: Boolean = false): EditText {
            return EditText(context).apply {
                hint = hintText
                textSize = 13.5f
                setSingleLine(true)
                if (isPassword) {
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                setPadding(26, 20, 26, 20)
                setTextColor(Color.parseColor("#202124"))
                setHintTextColor(Color.parseColor("#80868B"))
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#F1F3F4"))
                    cornerRadius = 16f
                    setStroke(2, Color.parseColor("#DADCE0"))
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
            }
        }

        val fullNameInput = createStyledInput("Full Name (e.g. Juan Dela Cruz)").apply {
            visibility = View.GONE
        }
        val usernameInput = createStyledInput("Username or Email")
        val passwordInput = createStyledInput("Password", isPassword = true)
        val confirmPasswordInput = createStyledInput("Confirm Password", isPassword = true).apply {
            visibility = View.GONE
        }

        // Role Selection (Visible only in Create Account mode)
        val roleContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        val roleTitle = TextView(context).apply {
            text = "Select Account Role:"
            textSize = 11.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#3C4043"))
            setPadding(4, 0, 0, 6)
        }

        val roleButtonsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val roles = listOf("Local Farmer", "Agri Planner", "Researcher")
        val roleButtons = mutableMapOf<String, Button>()

        fun updateRoleButtonStyles() {
            for ((rName, btn) in roleButtons) {
                val active = rName == selectedRole
                btn.background = GradientDrawable().apply {
                    setColor(if (active) Color.parseColor("#137333") else Color.parseColor("#E8EAED"))
                    cornerRadius = 14f
                }
                btn.setTextColor(if (active) Color.WHITE else Color.parseColor("#3C4043"))
            }
        }

        for (r in roles) {
            val rBtn = Button(context).apply {
                text = r
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setPadding(8, 10, 8, 10)
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
            setTextColor(Color.parseColor("#D93025"))
            visibility = View.GONE
            gravity = Gravity.CENTER
            setPadding(0, 2, 0, 10)
        }

        val btnPrimaryAction = Button(context).apply {
            text = "SIGN IN"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(24, 20, 24, 20)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#137333"))
                cornerRadius = 20f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 10)
            }
        }

        val btnToggleAuthPrompt = Button(context).apply {
            text = "Don't have an account? Create Account"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#137333"))
            setPadding(16, 12, 16, 12)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E6F4EA"))
                cornerRadius = 16f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 10)
            }
        }

        val btnGuest = Button(context).apply {
            text = "CONTINUE AS GUEST"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#1A73E8"))
            setPadding(16, 14, 16, 14)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E8F0FE"))
                cornerRadius = 16f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                errorLabel.visibility = View.GONE
                onLoginSuccess(
                    "Public Visitor (Guest)",
                    "Pindutin kahit saan sa mapa para sa GPS coordinates"
                )
            }
        }

        val hintNote = TextView(context).apply {
            text = "Demo Accounts: admin / admin123  or  farmer / nasugbu2026"
            textSize = 10.5f
            setTextColor(Color.parseColor("#80868B"))
            gravity = Gravity.CENTER
            setPadding(0, 14, 0, 0)
        }

        fun syncAuthModeUI() {
            errorLabel.visibility = View.GONE
            if (isRegisterMode) {
                title.text = "CREATE ACCOUNT"
                subtitle.text = "Register a new Nasugbu Agricultural GIS Account"
                fullNameInput.visibility = View.VISIBLE
                confirmPasswordInput.visibility = View.VISIBLE
                roleContainer.visibility = View.VISIBLE
                btnPrimaryAction.text = "CREATE ACCOUNT & SIGN IN"
                btnToggleAuthPrompt.text = "Already have an account? Sign In"
                hintNote.visibility = View.GONE

                tabCreateAcc.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#137333"))
                    cornerRadius = 16f
                }
                tabCreateAcc.setTextColor(Color.WHITE)

                tabSignIn.background = null
                tabSignIn.setTextColor(Color.parseColor("#5F6368"))
            } else {
                title.text = "SIGN IN"
                subtitle.text = "Nasugbu Municipal Agricultural GIS System (2026)"
                fullNameInput.visibility = View.GONE
                confirmPasswordInput.visibility = View.GONE
                roleContainer.visibility = View.GONE
                btnPrimaryAction.text = "SIGN IN"
                btnToggleAuthPrompt.text = "Don't have an account? Create Account"
                hintNote.visibility = View.VISIBLE

                tabSignIn.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#137333"))
                    cornerRadius = 16f
                }
                tabSignIn.setTextColor(Color.WHITE)

                tabCreateAcc.background = null
                tabCreateAcc.setTextColor(Color.parseColor("#5F6368"))
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
                        errorLabel.text = "Pakipunan ang lahat ng detalye para makagawa ng account."
                        errorLabel.visibility = View.VISIBLE
                    }
                    userKey == "admin" || userKey == "farmer" || prefs.contains("pwd_$userKey") -> {
                        errorLabel.text = "Ginagamit na ang username na '$userRaw'. Pumili ng iba."
                        errorLabel.visibility = View.VISIBLE
                    }
                    pass.length < 4 -> {
                        errorLabel.text = "Ang password ay dapat hindi bababa sa 4 na characters."
                        errorLabel.visibility = View.VISIBLE
                    }
                    pass != confirmPass -> {
                        errorLabel.text = "Hindi magkatugma ang Password at Confirm Password."
                        errorLabel.visibility = View.VISIBLE
                    }
                    else -> {
                        errorLabel.visibility = View.GONE
                        prefs.edit()
                            .putString("pwd_$userKey", pass)
                            .putString("name_$userKey", fullName)
                            .putString("role_$userKey", selectedRole)
                            .apply()

                        val displayRole = "$selectedRole ($fullName)"
                        fullNameInput.setText("")
                        usernameInput.setText("")
                        passwordInput.setText("")
                        confirmPasswordInput.setText("")
                        isRegisterMode = false
                        syncAuthModeUI()

                        Toast.makeText(context, "Account created! Welcome, $displayRole!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(displayRole, "Active: $displayRole • Tap anywhere for GPS")
                    }
                }
            } else {
                when {
                    userRaw.isEmpty() || pass.isEmpty() -> {
                        errorLabel.text = "Pakilagay ang iyong username at password."
                        errorLabel.visibility = View.VISIBLE
                    }
                    userKey == "admin" && pass == "admin123" -> {
                        errorLabel.visibility = View.GONE
                        val role = "Municipal Agri Planner (Admin)"
                        usernameInput.setText("")
                        passwordInput.setText("")
                        Toast.makeText(context, "Welcome back, $role!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(role, "Active: $role • Tap anywhere for GPS")
                    }
                    userKey == "farmer" && pass == "nasugbu2026" -> {
                        errorLabel.visibility = View.GONE
                        val role = "Local Farmer (Nasugbu)"
                        usernameInput.setText("")
                        passwordInput.setText("")
                        Toast.makeText(context, "Welcome back, $role!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(role, "Active: $role • Tap anywhere for GPS")
                    }
                    prefs.contains("pwd_$userKey") -> {
                        val savedPass = prefs.getString("pwd_$userKey", "")
                        if (savedPass == pass) {
                            errorLabel.visibility = View.GONE
                            val savedName = prefs.getString("name_$userKey", userRaw) ?: userRaw
                            val savedRole = prefs.getString("role_$userKey", "Registered User") ?: "Registered User"
                            val displayRole = "$savedRole ($savedName)"
                            usernameInput.setText("")
                            passwordInput.setText("")
                            Toast.makeText(context, "Welcome back, $displayRole!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(displayRole, "Active: $displayRole • Tap anywhere for GPS")
                        } else {
                            errorLabel.text = "Maling password para sa account na '$userRaw'."
                            errorLabel.visibility = View.VISIBLE
                        }
                    }
                    else -> {
                        errorLabel.text = "Walang nahanap na account. Pindutin ang 'Create Account' kung bago ka."
                        errorLabel.visibility = View.VISIBLE
                    }
                }
            }
        }

        syncAuthModeUI()

        card.addView(logoView)
        card.addView(title)
        card.addView(subtitle)
        card.addView(tabRow)
        card.addView(fullNameInput)
        card.addView(usernameInput)
        card.addView(passwordInput)
        card.addView(confirmPasswordInput)
        card.addView(roleContainer)
        card.addView(errorLabel)
        card.addView(btnPrimaryAction)
        card.addView(btnToggleAuthPrompt)
        card.addView(btnGuest)
        card.addView(hintNote)

        scrollContent.addView(card)
        scrollView.addView(scrollContent)
        outerLayout.addView(scrollView)
        return outerLayout
    }
}