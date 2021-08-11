package com.antiless.support.business.animxml

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.antiless.support.R
import kotlinx.android.synthetic.main.activity_anim_xml.blue
import kotlinx.android.synthetic.main.activity_anim_xml.orange
import kotlinx.android.synthetic.main.activity_anim_xml.red

class AnimXmlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anim_xml)

        startAnim()
    }

    private fun startAnim() {
        applyThreeBodyAnimation(orange, blue, red)
    }
}
