// TestActivity.kt
package com.example.conversionsapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TestActivity : AppCompatActivity() {

    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        val buttonCounter: Button = findViewById(R.id.buttonCounter)
        buttonCounter.text = "Count: $counter"

        buttonCounter.setOnClickListener {
            counter++
            buttonCounter.text = "Count: $counter"
        }
    }
}
