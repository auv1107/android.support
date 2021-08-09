package com.antiless.support.business.devicelayout

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.antiless.support.R
import kotlinx.android.synthetic.main.activity_devices_layout.arcLayout
import kotlinx.android.synthetic.main.activity_devices_layout.btn
import kotlinx.android.synthetic.main.activity_devices_layout.btn2
import kotlinx.android.synthetic.main.activity_devices_layout.rawArc

class DevicesLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices_layout)

        btn.setOnClickListener {
            arcLayout.addItem(View(this).apply {
                setBackgroundColor(Color.GREEN)
                layoutParams = ViewGroup.LayoutParams(100, 100)
            })
        }
        btn2.setOnClickListener {
            arcLayout.setCurrentIndex(2)
        }
    }
}
