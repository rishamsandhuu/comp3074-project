package com.example.huntquest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // apply splash theme before setContentView to avoid flicker
        setTheme(R.style.Theme_HuntQuest_Splash)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // show for ~3 seconds; change to 5000L for ~5s
        lifecycleScope.launch {
            delay(3000L)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}
