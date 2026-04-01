package fr.myefrei.nanoorbit.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.myefrei.nanoorbit.ui.theme.NanoOrbitTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DashboardScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "NanoOrbit Ground Control")
                }
            )
        }
    ) {
        Text(
            text = "Welcome to the NanoOrbit Ground Control Dashboard!",
            modifier = Modifier.padding(it)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    NanoOrbitTheme {
        DashboardScreen()
    }
}
