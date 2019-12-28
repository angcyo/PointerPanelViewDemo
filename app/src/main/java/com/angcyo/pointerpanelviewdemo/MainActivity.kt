package com.angcyo.pointerpanelviewdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        first_view.onPointerTextConfig = { degrees, progress, config ->
            when (progress) {
                0f -> {
                    config.text = "0°C"
                    config.textOffsetX = -10 * dpi
                    config.textOffsetY = 8 * dpi
                }
                1f -> {
                    config.text = "65°C"
                    config.textOffsetX = 10 * dpi
                }
                0.5f -> {
                    config.text = "45°C"
                    config.textOffsetY = -8 * dpi
                }
            }
        }

        second_view.isEnabled = false
        second_view.onPointerTextConfig = { degrees, progress, config ->
            Log.i("angcyo", "$progress")
            when (progress) {
                0.1f -> {
                    config.text = "25°C"
                }
                0.3f -> {
                    config.text = "35°C"
                }
                0.5f -> {
                    config.text = "45°C"
                }
                0.7f -> {
                    config.text = "55°C"
                }
                0.9f -> {
                    config.text = "65°C"
                }
            }
        }


        first_view.onProgressChange = { progress, fromUser ->
            if (fromUser) {
                second_view.setProgress(progress)
            }
            text_view.text = "${(65 * progress).toInt()}°C"
        }
    }
}
