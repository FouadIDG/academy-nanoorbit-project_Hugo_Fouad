package fr.myefrei.nanoorbit.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.myefrei.nanoorbit.data.mock.MockData
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.SatelliteInstrument
import fr.myefrei.nanoorbit.data.models.SatelliteMissionAssignment
import fr.myefrei.nanoorbit.ui.components.InstrumentItem
import fr.myefrei.nanoorbit.ui.components.StatusBadge
import fr.myefrei.nanoorbit.ui.theme.NanoOrbitTheme
import fr.myefrei.nanoorbit.viewmodel.NanoOrbitViewModel
import java.time.LocalDate

@Composable
fun DetailScreen(
    satelliteId: String,
    viewModel: NanoOrbitViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val satellite by viewModel.selectedSatellite.collectAsStateWithLifecycle()
    val instruments by viewModel.selectedSatelliteInstruments.collectAsStateWithLifecycle()
    val missions by viewModel.selectedSatelliteMissions.collectAsStateWithLifecycle()
    val favoriteSatelliteIds by viewModel.favoriteSatelliteIds.collectAsStateWithLifecycle()

    LaunchedEffect(satelliteId) {
        viewModel.loadSatelliteDetail(satelliteId)
    }

    DetailContent(
        satellite = satellite,
        instruments = instruments,
        missions = missions,
        remainingLifeMonths = satellite?.let(viewModel::estimateRemainingLifetimeMonths) ?: 0,
        orbitLabel = satellite?.idOrbite
            ?.let(viewModel::getOrbite)
            ?.let { orbite -> "${orbite.typeOrbite.libelleOracle} · ${orbite.altitudeKm} km" }
            ?: "Orbite inconnue",
        isFavorite = satelliteId in favoriteSatelliteIds,
        onFavoriteClick = { viewModel.toggleFavoriteSatellite(satelliteId) },
        modifier = modifier,
        onBackClick = onBackClick
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailContent(
    satellite: Satellite?,
    instruments: List<SatelliteInstrument>,
    missions: List<SatelliteMissionAssignment>,
    remainingLifeMonths: Int,
    orbitLabel: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    var anomalyText by remember { mutableStateOf("") }
    var anomalyError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = satellite?.nomSatellite ?: "Fiche satellite"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.StarBorder
                            },
                            contentDescription = if (isFavorite) {
                                "Retirer des favoris"
                            } else {
                                "Ajouter aux favoris"
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (satellite == null) {
            Text(
                text = "Satellite introuvable.",
                modifier = Modifier.padding(innerPadding).padding(24.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusSection(satellite = satellite, orbitLabel = orbitLabel)
            TelemetrySection(
                satellite = satellite,
                remainingLifeMonths = remainingLifeMonths
            )
            InstrumentsSection(instruments = instruments)
            MissionsSection(missions = missions)

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { isDialogOpen = true }
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Signaler une anomalie")
            }
        }
    }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                isDialogOpen = false
                anomalyText = ""
                anomalyError = null
            },
            title = {
                Text(text = "Signaler une anomalie")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = anomalyText,
                        onValueChange = {
                            anomalyText = it
                            anomalyError = null
                        },
                        label = { Text(text = "Description") },
                        supportingText = {
                            anomalyError?.let { message ->
                                Text(
                                    text = message,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (anomalyText.isBlank()) {
                            anomalyError = "La description est obligatoire."
                        } else {
                            isDialogOpen = false
                            anomalyText = ""
                            anomalyError = null
                        }
                    }
                ) {
                    Text(text = "Envoyer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isDialogOpen = false
                        anomalyText = ""
                        anomalyError = null
                    }
                ) {
                    Text(text = "Annuler")
                }
            }
        )
    }
}

@Composable
private fun StatusSection(
    satellite: Satellite,
    orbitLabel: String,
    modifier: Modifier = Modifier
) {
    InfoCard(modifier = modifier, title = "Statut") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = satellite.idSatellite, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Format ${satellite.formatCubesat.libelleOracle}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = orbitLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(statut = satellite.statut)
        }
    }
}

@Composable
private fun TelemetrySection(
    satellite: Satellite,
    remainingLifeMonths: Int,
    modifier: Modifier = Modifier
) {
    val batteryRatio = (satellite.capaciteBatterieWh / 100.0).coerceIn(0.0, 1.0).toFloat()

    InfoCard(modifier = modifier, title = "Télémétrie") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Masse : ${satellite.masseKg} kg")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Batterie : ${satellite.capaciteBatterieWh} Wh")
            }
            LinearProgressIndicator(
                progress = { batteryRatio },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Durée de vie restante estimée : $remainingLifeMonths mois",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InstrumentsSection(
    instruments: List<SatelliteInstrument>,
    modifier: Modifier = Modifier
) {
    InfoCard(modifier = modifier, title = "Instruments embarqués") {
        if (instruments.isEmpty()) {
            Text(text = "Aucun instrument embarqué.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                instruments.forEach { item ->
                    InstrumentItem(
                        instrument = item.instrument,
                        etatFonctionnement = item.etatFonctionnement.libelleOracle
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionsSection(
    missions: List<SatelliteMissionAssignment>,
    modifier: Modifier = Modifier
) {
    InfoCard(modifier = modifier, title = "Missions") {
        val activeMissions = missions.filter { assignment ->
            assignment.mission.dateFin == null ||
                !assignment.mission.dateFin.isBefore(LocalDate.now())
        }

        if (activeMissions.isEmpty()) {
            Text(text = "Aucune mission active.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                activeMissions.forEach { assignment ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = assignment.mission.nomMission,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = assignment.roleSatellite,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = assignment.mission.zoneGeoCible,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailContentPreview() {
    NanoOrbitTheme {
        DetailContent(
            satellite = MockData.satellites.first(),
            instruments = listOf(
                SatelliteInstrument(
                    instrument = MockData.instruments.first(),
                    etatFonctionnement = MockData.embarquements.first().etatFonctionnement
                )
            ),
            missions = listOf(
                SatelliteMissionAssignment(
                    mission = MockData.missions.first(),
                    roleSatellite = "Imageur principal"
                )
            ),
            remainingLifeMonths = 18,
            orbitLabel = "SSO · 550 km",
            isFavorite = true,
            onFavoriteClick = {},
            onBackClick = {}
        )
    }
}
