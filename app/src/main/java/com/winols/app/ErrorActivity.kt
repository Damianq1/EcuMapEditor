package com.winols.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(android.graphics.Color.parseColor("#121212"))
        }

        val title = TextView(this).apply {
            text = "⚠️ Aplikacja uległa awarii"
            setTextColor(android.graphics.Color.RED)
            textSize = 20f
            setPadding(0, 0, 0, 20)
        }

        val errorText = TextView(this).apply {
            text = intent.getStringExtra("CRASH_DETAILS") ?: "Brak szczegółów"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 12f
        }

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            addView(errorText)
        }

        val copyButton = Button(this).apply {
            text = "Kopiuj błąd do schowka"
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("CrashLog", errorText.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@ErrorActivity, "Skopiowano do schowka!", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(title)
        layout.addView(scroll)
        layout.addView(copyButton)
        setContentView(layout)
    }
}
