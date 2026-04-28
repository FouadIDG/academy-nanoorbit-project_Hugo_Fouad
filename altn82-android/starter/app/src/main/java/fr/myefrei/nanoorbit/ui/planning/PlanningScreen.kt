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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
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
import fr.myefrei.nanoorbit.data.models.PendingSyncStatus
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.StationSol
import fr.myefrei.nanoorbit.ui.components.CacheStatusBanner
import fr.myefrei.nanoorbit.ui.components.FenetreCard
import fr.myefrei.nanoorbit.ui.theme.NanoOrbitTheme
import fr.myefrei.nanoorbit.viewmodel.NanoOrbitViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    val pendingFenetres by viewModel.pendingFenetres.collectAsStateWithLifecycle()

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
        pendingCount = pendingFenetres.count { item -> item.status == PendingSyncStatus.PENDING },
        failedPendingCount = pendingFenetres.count { item -> item.status == PendingSyncStatus.FAILED },
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
    pendingCount: Int,
    failedPendingCount: Int,
    onStationFilterChange: (String?) -> Unit,
    onValidatePlanning: (String, String, LocalDateTime, Int, Double) -> Unit,
    onRetry: () -> Unit,
    onDismissMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDurationSeconds = fenetres.sumOf { fenetre -> fenetre.dureeSecondes }
    val totalVolumeMb = fenetres.sumOf { fenetre -> fenetre.volumeDonneesMb ?: 0.0 }
    var selectedSatelliteId by rememberSaveable { mutableStateOf("") }
    var dateInput by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var timeInput by rememberSaveable {
        mutableStateOf(
            LocalTime.now()
                .plusMinutes(10)
                .withSecond(0)
                .withNano(0)
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        )
    }
    var dureeSecondesInput by rememberSaveable { mutableStateOf("300") }
    var elevationMaxInput by rememberSaveable { mutableStateOf("60") }
    var inputErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isValidationSheetOpen by rememberSaveable { mutableStateOf(false) }
    val validationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val displayedErrorMessage = inputErrorMessage ?: errorMessage
    val clearMessages = {
        inputErrorMessage = null
        onDismissMessages()
    }

    LaunchedEffect(satellites) {
        if (selectedSatelliteId.isBlank() && satellites.isNotEmpty()) {
            selectedSatelliteId = satellites.first().idSatellite
        }
    }

    val stationNamesByCode = remember(stations) {
        stations.associate { station -> station.codeStation to station.nomStation }
    }
    val pendingLabel = when {
        failedPendingCount > 0 -> " · $failedPendingCount en erreur"
        pendingCount > 0 -> " · $pendingCount en attente"
        else -> ""
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
                    text = "${fenetres.size} fenêtre(s) · ${formatDuration(totalDurationSeconds)} · ${totalVolumeMb.toInt()} MB$pendingLabel",
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

                ValidationEntryPoint(
                    selectedSatelliteId = selectedSatelliteId,
                    selectedStationCode = selectedStationCode,
                    dateInput = dateInput,
                    timeInput = timeInput,
                    dureeSecondesInput = dureeSecondesInput,
                    errorMessage = displayedErrorMessage,
                    validationMessage = validationMessage,
                    onOpenValidation = { isValidationSheetOpen = true },
                    onDismissMessages = clearMessages
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (displayedErrorMessage != null && fenetres.isEmpty() && !isLoading) {
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

    if (isValidationSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isValidationSheetOpen = false },
            sheetState = validationSheetState
        ) {
            ValidationCard(
                satellites = satellites,
                stations = stations,
                selectedStationCode = selectedStationCode,
                selectedSatelliteId = selectedSatelliteId,
                dateInput = dateInput,
                timeInput = timeInput,
                dureeSecondesInput = dureeSecondesInput,
                elevationMaxInput = elevationMaxInput,
                errorMessage = displayedErrorMessage,
                validationMessage = validationMessage,
                onSatelliteChange = { selectedSatelliteId = it },
                onDateChange = { dateInput = it },
                onTimeChange = { timeInput = it },
                onDurationChange = { dureeSecondesInput = it },
                onElevationChange = { elevationMaxInput = it },
                onValidatePlanning = {
                    clearMessages()
                    val stationCode = selectedStationCode ?: stations
                        .firstOrNull()
                        ?.codeStation
                        .orEmpty()
                    val datetimeDebut = parsePlanningDateTime(dateInput, timeInput)
                    val dureeSecondes = dureeSecondesInput.toIntOrNull()
                    val elevationMax = elevationMaxInput.toDoubleOrNull()

                    when {
                        stationCode.isBlank() -> {
                            inputErrorMessage = "Choisis une station avant de planifier."
                        }
                        selectedSatelliteId.isBlank() -> {
                            inputErrorMessage = "Choisis un satellite avant de planifier."
                        }
                        datetimeDebut == null -> {
                            inputErrorMessage = "Date ou heure invalide. Formats attendus : 2026-04-28 et 14:30."
                        }
                        dureeSecondes == null -> {
                            inputErrorMessage = "Durée invalide : saisis un nombre de secondes."
                        }
                        elevationMax == null -> {
                            inputErrorMessage = "Élévation invalide : saisis un nombre de degrés."
                        }
                        else -> {
                            onValidatePlanning(
                                selectedSatelliteId,
                                stationCode,
                                datetimeDebut,
                                dureeSecondes,
                                elevationMax
                            )
                        }
                    }
                },
                onDismissMessages = clearMessages,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
            )
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
private fun ValidationEntryPoint(
    selectedSatelliteId: String,
    selectedStationCode: String?,
    dateInput: String,
    timeInput: String,
    dureeSecondesInput: String,
    errorMessage: String?,
    validationMessage: String?,
    onOpenValidation: () -> Unit,
    onDismissMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Nouvelle fenêtre",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${selectedSatelliteId.ifBlank { "Satellite" }} · ${selectedStationCode ?: "station"} · $dateInput $timeInput · ${dureeSecondesInput.ifBlank { "-" }} s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(onClick = onOpenValidation) {
                    Text(text = "Planifier")
                }
            }

            validationMessage?.let { message ->
                InlinePlanningMessage(
                    message = message,
                    isError = false,
                    onDismiss = onDismissMessages
                )
            }

            errorMessage?.let { message ->
                InlinePlanningMessage(
                    message = message,
                    isError = true,
                    onDismiss = onDismissMessages
                )
            }
        }
    }
}

@Composable
private fun InlinePlanningMessage(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = if (isError) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onDismiss) {
                Text(text = "OK")
            }
        }
    }
}

@Composable
private fun ValidationCard(
    satellites: List<Satellite>,
    stations: List<StationSol>,
    selectedStationCode: String?,
    selectedSatelliteId: String,
    dateInput: String,
    timeInput: String,
    dureeSecondesInput: String,
    elevationMaxInput: String,
    errorMessage: String?,
    validationMessage: String?,
    onSatelliteChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onElevationChange: (String) -> Unit,
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
                text = "Planifier une fenêtre",
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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = onDateChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(text = "Date") },
                    placeholder = { Text(text = "2026-04-28") }
                )

                OutlinedTextField(
                    value = timeInput,
                    onValueChange = onTimeChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(text = "Heure") },
                    placeholder = { Text(text = "14:30") }
                )
            }

            OutlinedTextField(
                value = dureeSecondesInput,
                onValueChange = onDurationChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = "Durée en secondes [1, 900]") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = elevationMaxInput,
                onValueChange = onElevationChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = "Élévation max en degrés [0, 90]") },
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
                    Text(text = "Planifier")
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

private fun parsePlanningDateTime(
    dateInput: String,
    timeInput: String
): LocalDateTime? {
    return runCatching {
        LocalDateTime.of(
            LocalDate.parse(dateInput.trim()),
            LocalTime.parse(timeInput.trim())
        )
    }.getOrNull()
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
            validationMessage = "Fenêtre planifiée pour SAT-001 depuis GS-KIR-01.",
            pendingCount = 1,
            failedPendingCount = 0,
            onStationFilterChange = {},
            onValidatePlanning = { _, _, _, _, _ -> },
            onRetry = {},
            onDismissMessages = {}
        )
    }
}
