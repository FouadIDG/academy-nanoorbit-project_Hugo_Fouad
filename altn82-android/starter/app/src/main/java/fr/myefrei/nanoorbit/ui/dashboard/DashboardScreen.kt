package fr.myefrei.nanoorbit.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.myefrei.nanoorbit.data.mock.MockData
import fr.myefrei.nanoorbit.data.models.Orbite
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.StatutSatellite
import fr.myefrei.nanoorbit.ui.components.CacheStatusBanner
import fr.myefrei.nanoorbit.ui.components.SatelliteCard
import fr.myefrei.nanoorbit.ui.theme.NanoOrbitTheme
import fr.myefrei.nanoorbit.viewmodel.NanoOrbitViewModel

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: NanoOrbitViewModel = viewModel(),
    onSatelliteClick: (String) -> Unit = {}
) {
    val satellites by viewModel.filteredSatellites.collectAsStateWithLifecycle()
    val orbites by viewModel.orbites.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatut by viewModel.selectedStatut.collectAsStateWithLifecycle()
    val isCacheMode by viewModel.isSatelliteCacheMode.collectAsStateWithLifecycle()
    val isMockMode by viewModel.isSatelliteMockMode.collectAsStateWithLifecycle()
    val cacheAgeLabel by viewModel.satelliteCacheAgeLabel.collectAsStateWithLifecycle()
    val favoriteSatelliteIds by viewModel.favoriteSatelliteIds.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()

    DashboardContent(
        modifier = modifier.padding(contentPadding),
        satellites = satellites,
        orbitesById = orbites.associateBy { orbite -> orbite.idOrbite },
        isLoading = isLoading,
        errorMessage = errorMessage,
        searchQuery = searchQuery,
        selectedStatut = selectedStatut,
        isCacheMode = isCacheMode,
        isMockMode = isMockMode,
        cacheAgeLabel = cacheAgeLabel,
        favoriteSatelliteIds = favoriteSatelliteIds,
        showFavoritesOnly = showFavoritesOnly,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onStatutFilterChange = viewModel::onStatutFilterChange,
        onShowFavoritesOnlyChange = viewModel::onShowFavoritesOnlyChange,
        onToggleFavorite = viewModel::toggleFavoriteSatellite,
        onRetry = viewModel::refreshSatellites,
        onSatelliteClick = onSatelliteClick
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DashboardContent(
    modifier: Modifier = Modifier,
    satellites: List<Satellite>,
    orbitesById: Map<Int, Orbite>,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    selectedStatut: StatutSatellite?,
    isCacheMode: Boolean,
    isMockMode: Boolean,
    cacheAgeLabel: String?,
    favoriteSatelliteIds: Set<String>,
    showFavoritesOnly: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onStatutFilterChange: (StatutSatellite?) -> Unit,
    onShowFavoritesOnlyChange: (Boolean) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRetry: () -> Unit,
    onSatelliteClick: (String) -> Unit
) {
    val nbOperationnels = satellites.count { satellite ->
        satellite.statut == StatutSatellite.OPERATIONNEL
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NanoOrbit Ground Control",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "Rechercher un satellite ou une orbite") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                StatusFilterBar(
                    selectedStatut = selectedStatut,
                    showFavoritesOnly = showFavoritesOnly,
                    onStatutFilterChange = onStatutFilterChange,
                    onShowFavoritesOnlyChange = onShowFavoritesOnlyChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isCacheMode || isMockMode) {
                    CacheStatusBanner(
                        title = if (isMockMode) "Mode démonstration" else "Mode hors-ligne",
                        cacheAgeLabel = cacheAgeLabel,
                        isMockMode = isMockMode
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "$nbOperationnels/${satellites.size} satellites opérationnels",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${satellites.size} résultat(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isLoading -> LoadingState()
                    errorMessage != null && satellites.isEmpty() -> ErrorState(
                        message = errorMessage,
                        onRetry = onRetry
                    )
                    else -> SatelliteList(
                        satellites = satellites,
                        orbitesById = orbitesById,
                        favoriteSatelliteIds = favoriteSatelliteIds,
                        onToggleFavorite = onToggleFavorite,
                        onSatelliteClick = onSatelliteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusFilterBar(
    selectedStatut: StatutSatellite?,
    showFavoritesOnly: Boolean,
    onStatutFilterChange: (StatutSatellite?) -> Unit,
    onShowFavoritesOnlyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedStatut == null,
                onClick = { onStatutFilterChange(null) },
                label = { Text(text = "Tous") }
            )
        }

        item {
            FilterChip(
                selected = showFavoritesOnly,
                onClick = { onShowFavoritesOnlyChange(!showFavoritesOnly) },
                label = { Text(text = "Favoris") }
            )
        }

        items(StatutSatellite.entries) { statut ->
            FilterChip(
                selected = selectedStatut == statut,
                onClick = { onStatutFilterChange(statut) },
                label = { Text(text = statut.libelleOracle) }
            )
        }
    }
}

@Composable
private fun SatelliteList(
    satellites: List<Satellite>,
    orbitesById: Map<Int, Orbite>,
    favoriteSatelliteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSatelliteClick: (String) -> Unit
) {
    // Q1: LazyColumn compose uniquement les éléments visibles et recycle la mesure pendant
    // le scroll. Avec Column, 100 satellites seraient tous composés d'un coup, ce qui
    // augmente le coût CPU, la mémoire et peut provoquer des saccades ou un premier rendu lent.
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(
            items = satellites,
            key = { satellite -> satellite.idSatellite }
        ) { satellite ->
            val orbite = orbitesById[satellite.idOrbite]
            SatelliteCard(
                satellite = satellite,
                orbiteLabel = orbite?.let { item ->
                    "${item.typeOrbite.libelleOracle} · ${item.altitudeKm} km · ${item.zoneCouverture}"
                } ?: "Orbite inconnue",
                isFavorite = satellite.idSatellite in favoriteSatelliteIds,
                onFavoriteClick = { onToggleFavorite(satellite.idSatellite) },
                onClick = {
                    onSatelliteClick(satellite.idSatellite)
                }
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chargement de la constellation NanoOrbit...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) {
            Text(text = "Réessayer")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    NanoOrbitTheme {
        DashboardContent(
            satellites = MockData.satellites,
            orbitesById = MockData.orbitesById,
            isLoading = false,
            errorMessage = null,
            searchQuery = "",
            selectedStatut = null,
            isCacheMode = true,
            isMockMode = false,
            cacheAgeLabel = "Mis à jour il y a 3 min",
            favoriteSatelliteIds = setOf("SAT-001", "SAT-003"),
            showFavoritesOnly = false,
            onSearchQueryChange = {},
            onStatutFilterChange = {},
            onShowFavoritesOnlyChange = {},
            onToggleFavorite = {},
            onRetry = {},
            onSatelliteClick = {}
        )
    }
}
