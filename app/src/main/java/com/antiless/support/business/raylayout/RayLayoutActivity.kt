package com.antiless.support.business.raylayout

import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.antiless.support.R
import com.antiless.support.design.raylayout.RayDrawable
import kotlinx.android.synthetic.main.activity_ray_layout.blue
import kotlinx.android.synthetic.main.activity_ray_layout.contentView
import kotlinx.android.synthetic.main.activity_ray_layout.image
import kotlinx.android.synthetic.main.activity_ray_layout.red
import kotlinx.android.synthetic.main.activity_ray_layout.yellow

class RayLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ray_layout)
        ValueAnimator.ofFloat(0f, 1000f, 0f).apply {
            addUpdateListener {
                yellow.x = it.animatedValue as Float
            }
            duration = 2000
            repeatMode = ValueAnimator.RESTART
            repeatCount = -1
            start()
        }
        image.post {
            Log.i("RayLayoutActivity", "onCreate: width height ${image.width} ${image.height}")
            image.setImageDrawable(RayDrawable().apply {
                addRay(
                    Rect(0, 0, 100, 100),
                    Rect(1000, 1000, 1100, 1100),
                )
            })
        }
        contentView.post {
            contentView.overlay.add(RayDrawable().apply {
                addRay(
                    Rect(0, 0, 100, 100),
                    Rect(1000, 1000, 1100, 1100),
                ).apply {
                    start()
                }
                addRay(blue, red).apply {
                    duration = 3000
                    start()
                }
                addRay(blue, yellow).start()
            })
        }
    }
}
