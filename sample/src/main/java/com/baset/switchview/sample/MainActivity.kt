package com.baset.switchview.sample

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baset.switchview.OnCheckedChangeListener
import com.baset.switchview.SwitchView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        switch_preference.setOnCheckedChangeListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(view: SwitchView, isChecked: Boolean) {
                Snackbar.make(
                    rootView,
                    "SwitchView Value: ${switch_preference.isChecked()}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_set_checked -> {
                switch_preference.setChecked(true)
            }
            R.id.btn_set_unset -> {
                switch_preference.setChecked(false)
            }
        }
    }

    fun getColorRes(@ColorRes colorInt: Int): Int {
        return ContextCompat.getColor(baseContext, colorInt)
    }
}
