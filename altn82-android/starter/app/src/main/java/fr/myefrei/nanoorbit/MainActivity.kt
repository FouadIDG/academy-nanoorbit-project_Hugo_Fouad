package fr.myefrei.nanoorbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.myefrei.nanoorbit.ui.NanoOrbitApp
import fr.myefrei.nanoorbit.ui.theme.NanoOrbitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NanoOrbitTheme {
                NanoOrbitApp()
            }
        }
    }
}
