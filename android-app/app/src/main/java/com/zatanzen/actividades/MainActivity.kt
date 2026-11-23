package com.zatanzen.actividades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zatanzen.actividades.ui.AppNavigation
import com.zatanzen.actividades.ui.theme.ActividadesTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // OpenStreetMap tile servers require an identifying user agent.
        Configuration.getInstance().userAgentValue = packageName
        enableEdgeToEdge()
        setContent {
            ActividadesTheme {
                AppNavigation()
            }
        }
    }
}
