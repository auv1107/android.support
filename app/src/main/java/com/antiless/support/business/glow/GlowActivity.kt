package com.antiless.support.business.glow

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.antiless.support.R
import com.antiless.support.design.glow.GlowDrawable
import kotlinx.android.synthetic.main.activity_glow.glow

class GlowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glow)


        glow.setImageDrawable(GlowDrawable(Color.parseColor("#5724C789"), Color.parseColor("#5724C789")))
    }
}
