package com.antiless.support.business.raylayout

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.antiless.support.R
import com.antiless.support.design.raylayout.RayDrawable
import com.antiless.support.design.raylayout.addRay
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
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.images_line)
        image.post {
            Log.i("RayLayoutActivity", "onCreate: width height ${image.width} ${image.height}")
            image.setImageDrawable(RayDrawable(bitmap).apply {
                addRay(
                    RayDrawable.Circle(PointF(50f, 50f), 100f),
                    RayDrawable.Circle(PointF(1050f, 1050f), 100f),
                )
            })
        }
        contentView.post {
            contentView.overlay.add(RayDrawable(bitmap).apply {
                addRay(contentView, blue, red).apply {
                    with(info) {
                        duration = 3000
                        doOnEnd = {
                            Log.i("RayLayoutActivity", "onCreate: End")
                        }
                        start()
                    }
                }
                addRay(contentView, blue, yellow).apply {
                    with(info) {
                        duration = 3000
                        isPersistent = true
                        start()
                    }
                }
            })
        }
    }
}
