package com.macradar.ai.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.AnimationSet
import androidx.appcompat.app.AppCompatActivity
import com.macradar.ai.R
import com.macradar.ai.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo
        val scaleAnim = ScaleAnimation(
            0.5f, 1.0f, 0.5f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply { duration = 800 }

        val fadeAnim = AlphaAnimation(0f, 1f).apply { duration = 800 }

        val animSet = AnimationSet(true).apply {
            addAnimation(scaleAnim)
            addAnimation(fadeAnim)
        }

        binding.ivLogo.startAnimation(animSet)
        binding.tvAppName.startAnimation(AlphaAnimation(0f, 1f).apply { 
            duration = 1000
            startOffset = 400
            fillAfter = true
        })
        binding.tvTagline.startAnimation(AlphaAnimation(0f, 1f).apply {
            duration = 1000
            startOffset = 700
            fillAfter = true
        })

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 2500)
    }
}
