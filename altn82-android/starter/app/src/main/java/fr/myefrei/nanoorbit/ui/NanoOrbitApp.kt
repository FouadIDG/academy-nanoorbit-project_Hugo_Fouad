package fr.myefrei.nanoorbit.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.myefrei.nanoorbit.ui.dashboard.DashboardScreen

@Composable
fun NanoOrbitApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        DashboardScreen()
    }
}
