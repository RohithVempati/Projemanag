package com.pctipsguy.projemanag

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
        Handler(Looper.myLooper()!!).postDelayed({
            var currentUserId = FirestoreClass().getCurrentUserID()
            if(currentUserId.isNotEmpty())
                startActivity(Intent(this,MainActivity::class.java))
            else
                startActivity(Intent(this,IntroActivity::class.java))
            finish()
        },2000)
    }
}