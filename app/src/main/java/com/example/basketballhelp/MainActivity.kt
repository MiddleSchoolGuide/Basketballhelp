package com.example.basketballhelp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.basketballhelp.ui.app.HoopDevApp
import com.example.basketballhelp.ui.theme.HoopDevTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HoopDevTheme {
                HoopDevApp(app = application as HoopDevApplication)
            }
        }
    }
}
