package fr.myefrei.nanoorbit.ui.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.myefrei.nanoorbit.data.mock.MockData
import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.StationSol
import fr.myefrei.nanoorbit.ui.components.CacheStatusBanner
import fr.myefrei.nanoorbit.ui.components.FenetreCard
import fr.myefrei.nanoorbit.ui.theme.NanoOrbitTheme
import fr.myefrei.nanoorbit.viewmodel.NanoOrbitViewModel

@Composable
fun PlanningScreen(
    contentPadding: PaddingValues,
    viewModel: NanoOrbitViewModel,
    modifier: Modifier = Modifier
) {
    val fenetres by viewModel.filteredFenetres.collectAsStateWithLifecycle()
    val stations by viewModel.stationsSol.collectAsStateWithLifecycle()
    val satellites by viewModel.satellites.collectAsStateWithLifecycle()
    val selectedStationCode by viewModel.selectedStationCode.collectAsStateWithLifecycle()
    val isLoading by viewModel.isPlanningLoading.collectAsStateWithLifecycle()
    val isCacheMode by viewModel.isFenetreCacheMode.collectAsStateWithLifecycle()
    val isMockMode by viewModel.isFenetreMockMode.collectAsStateWithLifecycle()
    val cacheAgeLabel by viewModel.fenetreCacheAgeLabel.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val validationMessage by viewModel.planningValidationMessage.collectAsStateWithLifecycle()

    PlanningContent(
        modifier = modifier.padding(contentPadding),
        fenetres = fenetres,
        stations = stations,
        satellites = satellites,
        selectedStationCode = selectedStationCode,
        isLoading = isLoading,
        isCacheMode = isCacheMode,
        isMockMode = isMockMode,
        cacheAgeLabel = cacheAgeLabel,
        errorMessage = errorMessage,
        validationMessage = validationMessage,
        onStationFilterChange = viewModel::onStationFilterChange,
        onValidatePlanning = viewModel::validatePlanningRequest,
        onRetry = viewModel::refreshPlanning,
        onDismissMessages = {
            viewModel.clearErrorMessage()
            viewModel.clearPlanningValidationMessage()
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PlanningContent(
    fenetres: List<FenetreCom>,
    stations: List<StationSol>,
    satellites: List<Satellite>,
    selectedStationCode: String?,
    isLoading: Boolean,
    isCacheMode: Boolean,
    isMockMode: Boolean,
    cacheAgeLabel: String?,
    errorMessage: String?,
    validationMessage: String?,
    onStationFilterChange: (String?) -> Unit,
    onValidatePlanning: (String, String, Int) -> Unit,
    onRetry: () -> Unit,
    onDismissMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDurationSeconds = fenetres.sumOf { fenetre -> fenetre.dureeSecondes }
    val totalVolumeMb = fenetres.sumOf { fenetre -> fenetre.volumeDonneesMb ?: 0.0 }
    var selectedSatelliteId by rememberSaveable { mutableStateOf("") }
    var dureeSecondesInput by rememberSaveable { mutableStateOf("300") }

    LaunchedEffect(satellites) {
        if (selectedSatelliteId.isBlank() && satellites.isNotEmpty()) {
            selectedSatelliteId = satellites.first().idSatellite
        }
    }

    val stationNamesByCode = remember(stations) {
        stations.associate { station -> station.codeStation to station.nomStation }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Planning des communications",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRetry,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                if (isCacheMode || isMockMode) {
                    CacheStatusBanner(
                        title = if (isMockMode) "Mode démonstration" else "Mode hors-ligne",
                        cacheAgeLabel = cacheAgeLabel,
                        isMockMode = isMockMode
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "${fenetres.size} fenêtre(s) · ${formatDuration(totalDurationSeconds)} · ${totalVolumeMb.toInt()} MB",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                StationFilterBar(
                    stations = stations,
                    selectedStationCode = selectedStationCode,
                    onStationFilterChange = onStationFilterChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                ValidationCard(
                    satellites = satellites,
                    stations = stations,
                    selectedStationCode = selectedStationCode,
                    selectedSatelliteId = selectedSatelliteId,
                    dureeSecondesInput = dureeSecondesInput,
                    errorMessage = errorMessage,
                    validationMessage = validationMessage,
                    onSatelliteChange = { selectedSatelliteId = it },
                    onDurationChange = { dureeSecondesInput = it },
                    onValidatePlanning = {
                        onDismissMessages()
                        val stationCode = selectedStationCode ?: stations
                            .firstOrNull()
                            ?.codeStation
                            .orEmpty()
                        onValidatePlanning(
                            selectedSatelliteId,
                            stationCode,
                            dureeSecondesInput.toIntOrNull() ?: 0
                        )
                    },
                    onDismissMessages = onDismissMessages
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null && fenetres.isEmpty() && !isLoading) {
                    Button(onClick = onRetry) {
                        Text(text = "Réessayer")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = fenetres,
                            key = { fenetre -> fenetre.idFenetre }
                        ) { fenetre ->
                            FenetreCard(
                                fenetre = fenetre,
                                nomStation = fenetre.nomStation
                                    ?: stationNamesByCode[fenetre.codeStation]
                                    ?: fenetre.codeStation
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationFilterBar(
    stations: List<StationSol>,
    selectedStationCode: String?,
    onStationFilterChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedStationCode == null,
                onClick = { onStationFilterChange(null) },
                label = { Text(text = "Toutes") }
            )
        }

        items(stations, key = { station -> station.codeStation }) { station ->
            FilterChip(
                selected = selectedStationCode == station.codeStation,
                onClick = { onStationFilterChange(station.codeStation) },
                label = { Text(text = station.codeStation) }
            )
        }
    }
}

@Composable
private fun ValidationCard(
    satellites: List<Satellite>,
    stations: List<StationSol>,
    selectedStationCode: String?,
    selectedSatelliteId: String,
    dureeSecondesInput: String,
    errorMessage: String?,
    validationMessage: String?,
    onSatelliteChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onValidatePlanning: () -> Unit,
    onDismissMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Validation nouvelle fenêtre",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Station cible : ${selectedStationCode ?: stations.firstOrNull()?.codeStation ?: "Aucune"}",
                style = MaterialTheme.typography.bodyMedium
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(satellites, key = { satellite -> satellite.idSatellite }) { satellite ->
                    FilterChip(
                        selected = selectedSatelliteId == satellite.idSatellite,
                        onClick = { onSatelliteChange(satellite.idSatellite) },
                        label = { Text(text = satellite.idSatellite) }
                    )
                }
            }

            OutlinedTextField(
                value = dureeSecondesInput,
                onValueChange = onDurationChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = "Durée en secondes [1, 900]") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            validationMessage?.let { message ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            errorMessage?.let { message ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onValidatePlanning) {
                    Text(text = "Vérifier")
                }

                TextButton(onClick = onDismissMessages) {
                    Text(text = "Effacer")
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours} h ${minutes} min ${seconds} s"
        else -> "${minutes} min ${seconds} s"
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanningContentPreview() {
    NanoOrbitTheme {
        PlanningContent(
            fenetres = MockData.fenetresCom,
            stations = MockData.stationsSol,
            satellites = MockData.satellites,
            selectedStationCode = null,
            isLoading = false,
            isCacheMode = true,
            isMockMode = false,
            cacheAgeLabel = "Mis à jour il y a 6 min",
            errorMessage = null,
            validationMessage = "Fenêtre planifiable pour SAT-001 depuis GS-KIR-01.",
            onStationFilterChange = {},
            onValidatePlanning = { _, _, _ -> },
            onRetry = {},
            onDismissMessages = {}
        )
    }
}
