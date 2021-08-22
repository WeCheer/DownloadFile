package com.wyc.downloadfile.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.wyc.downloadfile.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun singleDownload(view: View) {
        startActivity(Intent(this, SingleActivity::class.java))
    }

    fun multipleDownload(view: View) {
        startActivity(Intent(this, MultipleActivity::class.java))
    }

    fun limitDownload(view: View) {
        startActivity(Intent(this, LimitActivity::class.java))
    }
}
