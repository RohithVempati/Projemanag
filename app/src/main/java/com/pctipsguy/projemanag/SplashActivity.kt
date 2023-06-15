package com.pctipsguy.projemanag

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pctipsguy.projemanag.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.windowInsetsController!!.hide(
            android.view.WindowInsets.Type.statusBars()
        )
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val typeface: Typeface =
            Typeface.createFromAsset(assets, "Sunnyspells.otf")
        binding?.tvAppName?.typeface = typeface
    }
}