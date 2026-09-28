package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class Farm(val name: String, val crop: String) {

    fun displayInfo(): String {
        return "$name\nCrop: $crop"
    }
}

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)

        val title = TextView(this)
        title.text = "NASUGBU CROP MAP"
        title.textSize = 24f
        title.gravity = Gravity.CENTER

        val button = Button(this)
        button.text = "DISPLAY MAP"

        val result = TextView(this)
        result.textSize = 18f
        result.setPadding(10, 20, 10, 10)

        val farms = arrayOf(
            Farm("Banilad", "Rice"),
            Farm("Dayap", "Sugarcane"),
            Farm("Bulihan", "Corn"),
            Farm("Cogunan", "Mango"),
            Farm("Putat", "Coconut")
        )

        button.setOnClickListener {

            var i = 0
            var info = ""

            while (i < farms.size) {
                info += farms[i].displayInfo() + "\n\n"
                i++
            }

            result.text = info
        }

        layout.addView(title)
        layout.addView(button)
        layout.addView(result)

        setContentView(layout)
    }
}