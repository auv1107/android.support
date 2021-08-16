package com.antiless.support

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.antiless.support.business.devicelayout.DevicesLayoutActivity
import com.antiless.support.business.main.adapter.MainActionAdapter
import com.antiless.support.business.main.utils.getMainActionModels
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
        startActivity(Intent(this, DevicesLayoutActivity::class.java))
    }
}
