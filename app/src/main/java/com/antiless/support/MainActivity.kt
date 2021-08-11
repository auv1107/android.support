package com.antiless.support

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.antiless.support.business.animxml.AnimXmlActivity
import com.antiless.support.business.glow.GlowActivity
import com.antiless.support.business.main.adapter.MainActionAdapter
import com.antiless.support.business.main.utils.getMainActionModels
import com.antiless.support.business.raylayout.RayLayoutActivity
import kotlinx.android.synthetic.main.activity_main.list

class MainActivity : AppCompatActivity() {
    private val mainActionAdapter = MainActionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        list.adapter = mainActionAdapter
        mainActionAdapter.setData(
            getMainActionModels()
        )
        startActivity(Intent(this, GlowActivity::class.java))
    }
}
