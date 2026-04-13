package fr.myefrei.nanoorbit.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import fr.myefrei.nanoorbit.data.mock.MockData
import fr.myefrei.nanoorbit.data.models.StatutStation
import fr.myefrei.nanoorbit.data.models.StationSol
import fr.myefrei.nanoorbit.ui.theme.NanoOrbitTheme
import fr.myefrei.nanoorbit.viewmodel.NanoOrbitViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.library.R as OsmdroidR

@Composable
fun MapScreen(
    contentPadding: PaddingValues,
    viewModel: NanoOrbitViewModel,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.stationsSol.collectAsStateWithLifecycle()

    MapContent(
        stations = stations,
        modifier = modifier.padding(contentPadding)
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MapContent(
    stations: List<StationSol>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var operatorLocation by remember { mutableStateOf<Location?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { granted -> granted }) {
            fetchLastLocation(
                context = context,
                onLocationFound = { location ->
                    operatorLocation = location
                    mapViewRef?.controller?.animateTo(
                        GeoPoint(location.latitude, location.longitude)
                    )
                }
            )
            permissionMessage = null
        } else {
            permissionMessage = "Permission GPS refusée."
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Carte des stations au sol")
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (hasLocationPermission(context)) {
                        fetchLastLocation(
                            context = context,
                            onLocationFound = { location ->
                                operatorLocation = location
                                mapViewRef?.controller?.animateTo(
                                    GeoPoint(location.latitude, location.longitude)
                                )
                            }
                        )
                        permissionMessage = null
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null
                    )
                },
                text = { Text(text = "Me localiser") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapContext ->
                    MapView(mapContext).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(3.5)
                        val center = stations.firstOrNull()?.let { station ->
                            GeoPoint(station.latitude, station.longitude)
                        } ?: GeoPoint(43.6047, 1.4442)
                        controller.setCenter(center)
                        mapViewRef = this
                    }
                },
                update = { mapView ->
                    mapViewRef = mapView
                    renderStationsOnMap(
                        mapView = mapView,
                        stations = stations,
                        operatorLocation = operatorLocation
                    )
                }
            )

            permissionMessage?.let { message ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(operatorLocation) {
        operatorLocation?.let { location ->
            mapViewRef?.controller?.animateTo(GeoPoint(location.latitude, location.longitude))
        }
    }
}

private fun renderStationsOnMap(
    mapView: MapView,
    stations: List<StationSol>,
    operatorLocation: Location?
) {
    mapView.overlays.clear()

    stations.forEach { station ->
        val stationPoint = GeoPoint(station.latitude, station.longitude)
        val marker = Marker(mapView).apply {
            position = stationPoint
            title = station.nomStation
            snippet = buildStationSnippet(station, stationPoint, operatorLocation)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(
                mapView.context,
                OsmdroidR.drawable.marker_default
            )?.mutate()?.apply {
                setTint(stationMarkerColor(station.statut))
            }
        }
        mapView.overlays.add(marker)
    }

    operatorLocation?.let { location ->
        mapView.overlays.add(
            Marker(mapView).apply {
                position = GeoPoint(location.latitude, location.longitude)
                title = "Ma position"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
        )
    }

    mapView.invalidate()
}

private fun buildStationSnippet(
    station: StationSol,
    stationPoint: GeoPoint,
    operatorLocation: Location?
): String {
    val distanceLabel = operatorLocation?.let { location ->
        val distanceKm = location.distanceTo(
            Location("station").apply {
                latitude = stationPoint.latitude
                longitude = stationPoint.longitude
            }
        ) / 1000.0
        " · ${"%.1f".format(distanceKm)} km"
    }.orEmpty()

    return "Bande ${station.bandeFrequence} · ${station.debitMaxMbps.toInt()} Mbps$distanceLabel"
}

private fun stationMarkerColor(statutStation: StatutStation): Int {
    return when (statutStation) {
        StatutStation.ACTIVE -> 0xFF1B5E20.toInt()
        StatutStation.MAINTENANCE -> 0xFFF57C00.toInt()
        StatutStation.INACTIVE -> 0xFF616161.toInt()
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}

private fun fetchLastLocation(
    context: Context,
    onLocationFound: (Location) -> Unit
) {
    if (!hasLocationPermission(context)) return

    LocationServices.getFusedLocationProviderClient(context)
        .lastLocation
        .addOnSuccessListener { location ->
            location?.let(onLocationFound)
        }
}

@Preview(showBackground = true)
@Composable
private fun MapContentPreview() {
    NanoOrbitTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MockData.stationsSol.take(3).forEach { station ->
                Text(text = "${station.nomStation} · ${station.statut.libelleOracle}")
            }
        }
    }
}
