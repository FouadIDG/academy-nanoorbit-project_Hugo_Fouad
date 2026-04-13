package fr.myefrei.nanoorbit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.myefrei.nanoorbit.NanoOrbitApplication
import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.Orbite
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.SatelliteInstrument
import fr.myefrei.nanoorbit.data.models.SatelliteMissionAssignment
import fr.myefrei.nanoorbit.data.models.StatutSatellite
import fr.myefrei.nanoorbit.data.models.StationSol
import fr.myefrei.nanoorbit.data.repository.NanoOrbitRepository
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NanoOrbitViewModel(
    private val repository: NanoOrbitRepository = NanoOrbitApplication.repository ?: NanoOrbitRepository()
) : ViewModel() {
    private val _satellites = MutableStateFlow<List<Satellite>>(emptyList())
    val satellites: StateFlow<List<Satellite>> = _satellites.asStateFlow()

    private val _orbites = MutableStateFlow<List<Orbite>>(emptyList())
    val orbites: StateFlow<List<Orbite>> = _orbites.asStateFlow()

    private val _stationsSol = MutableStateFlow<List<StationSol>>(emptyList())
    val stationsSol: StateFlow<List<StationSol>> = _stationsSol.asStateFlow()

    private val _fenetres = MutableStateFlow<List<FenetreCom>>(emptyList())
    val fenetres: StateFlow<List<FenetreCom>> = _fenetres.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPlanningLoading = MutableStateFlow(false)
    val isPlanningLoading: StateFlow<Boolean> = _isPlanningLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatut = MutableStateFlow<StatutSatellite?>(null)
    val selectedStatut: StateFlow<StatutSatellite?> = _selectedStatut.asStateFlow()

    private val _favoriteSatelliteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteSatelliteIds: StateFlow<Set<String>> = _favoriteSatelliteIds.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _selectedStationCode = MutableStateFlow<String?>(null)
    val selectedStationCode: StateFlow<String?> = _selectedStationCode.asStateFlow()

    private val _isSatelliteCacheMode = MutableStateFlow(false)
    val isSatelliteCacheMode: StateFlow<Boolean> = _isSatelliteCacheMode.asStateFlow()

    private val _isSatelliteMockMode = MutableStateFlow(false)
    val isSatelliteMockMode: StateFlow<Boolean> = _isSatelliteMockMode.asStateFlow()

    private val _satelliteCacheAgeLabel = MutableStateFlow<String?>(null)
    val satelliteCacheAgeLabel: StateFlow<String?> = _satelliteCacheAgeLabel.asStateFlow()

    private val _isFenetreCacheMode = MutableStateFlow(false)
    val isFenetreCacheMode: StateFlow<Boolean> = _isFenetreCacheMode.asStateFlow()

    private val _isFenetreMockMode = MutableStateFlow(false)
    val isFenetreMockMode: StateFlow<Boolean> = _isFenetreMockMode.asStateFlow()

    private val _fenetreCacheAgeLabel = MutableStateFlow<String?>(null)
    val fenetreCacheAgeLabel: StateFlow<String?> = _fenetreCacheAgeLabel.asStateFlow()

    private val _selectedSatellite = MutableStateFlow<Satellite?>(null)
    val selectedSatellite: StateFlow<Satellite?> = _selectedSatellite.asStateFlow()

    private val _selectedSatelliteInstruments =
        MutableStateFlow<List<SatelliteInstrument>>(emptyList())
    val selectedSatelliteInstruments: StateFlow<List<SatelliteInstrument>> =
        _selectedSatelliteInstruments.asStateFlow()

    private val _selectedSatelliteMissions =
        MutableStateFlow<List<SatelliteMissionAssignment>>(emptyList())
    val selectedSatelliteMissions: StateFlow<List<SatelliteMissionAssignment>> =
        _selectedSatelliteMissions.asStateFlow()

    private val _planningValidationMessage = MutableStateFlow<String?>(null)
    val planningValidationMessage: StateFlow<String?> =
        _planningValidationMessage.asStateFlow()

    private val satellitesWithOrbites = combine(
        _satellites,
        _orbites
    ) { satellites, orbites ->
        satellites to orbites.associateBy { orbite -> orbite.idOrbite }
    }

    val filteredSatellites: StateFlow<List<Satellite>> = combine(
        satellitesWithOrbites,
        _searchQuery,
        _selectedStatut,
        _favoriteSatelliteIds,
        _showFavoritesOnly
    ) { source, query, statut, favoriteIds, showFavoritesOnly ->
        val (satellites, orbitesById) = source

        satellites.filter { satellite ->
            val orbite = orbitesById[satellite.idOrbite]
            val matchesQuery = query.isBlank() ||
                satellite.nomSatellite.contains(query, ignoreCase = true) ||
                satellite.idSatellite.contains(query, ignoreCase = true) ||
                satellite.formatCubesat.libelleOracle.contains(query, ignoreCase = true) ||
                orbite?.typeOrbite?.libelleOracle?.contains(query, ignoreCase = true) == true
            val matchesStatut = statut == null || satellite.statut == statut
            val matchesFavorite = !showFavoritesOnly || satellite.idSatellite in favoriteIds

            matchesQuery && matchesStatut && matchesFavorite
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val filteredFenetres: StateFlow<List<FenetreCom>> = combine(
        _fenetres,
        _selectedStationCode
    ) { fenetres, stationCode ->
        fenetres
            .filter { fenetre ->
                stationCode == null || fenetre.codeStation == stationCode
            }
            .sortedBy { fenetre -> fenetre.datetimeDebut }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        loadOrbites()
        loadStations()
        loadSatellites()
        loadFenetres()
        observeFavoriteSatellites()
    }

    fun loadOrbites() {
        viewModelScope.launch {
            runCatching {
                _orbites.value = repository.getOrbites()
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    fun loadStations() {
        viewModelScope.launch {
            runCatching {
                _stationsSol.value = repository.getStationsSol()
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    fun loadSatellites() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.getSatellites()
                _satellites.value = result.data
                _isSatelliteCacheMode.value = result.fromCache
                _isSatelliteMockMode.value = result.fromMock
                _satelliteCacheAgeLabel.value = when {
                    result.fromCache -> formatCacheAgeLabel(result.lastUpdatedEpochMillis)
                    result.fromMock -> "API indisponible · données MockData locales"
                    else -> null
                }
            } catch (exception: Exception) {
                _isSatelliteCacheMode.value = false
                _isSatelliteMockMode.value = false
                _satelliteCacheAgeLabel.value = null
                _errorMessage.value = exception.message
                    ?: "Impossible de charger les satellites NanoOrbit."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFenetres() {
        viewModelScope.launch {
            _isPlanningLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.getFenetres()
                _fenetres.value = result.data
                _isFenetreCacheMode.value = result.fromCache
                _isFenetreMockMode.value = result.fromMock
                _fenetreCacheAgeLabel.value = when {
                    result.fromCache -> formatCacheAgeLabel(result.lastUpdatedEpochMillis)
                    result.fromMock -> "API indisponible · fenêtres MockData locales"
                    else -> null
                }
            } catch (exception: Exception) {
                _isFenetreCacheMode.value = false
                _isFenetreMockMode.value = false
                _fenetreCacheAgeLabel.value = null
                _errorMessage.value = exception.message
                    ?: "Impossible de charger le planning de communication."
            } finally {
                _isPlanningLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatutFilterChange(statut: StatutSatellite?) {
        _selectedStatut.value = statut
    }

    fun refreshSatellites() {
        loadOrbites()
        loadSatellites()
    }

    fun refreshPlanning() {
        loadStations()
        loadFenetres()
    }

    fun onStationFilterChange(codeStation: String?) {
        _selectedStationCode.value = codeStation
    }

    fun onShowFavoritesOnlyChange(showFavoritesOnly: Boolean) {
        _showFavoritesOnly.value = showFavoritesOnly
    }

    fun toggleFavoriteSatellite(satelliteId: String) {
        viewModelScope.launch {
            repository.toggleFavoriteSatellite(satelliteId)
        }
    }

    fun validateFenetreDuration(dureeSecondes: Int): Boolean {
        return repository.validateFenetreDuration(dureeSecondes)
            .onFailure { error -> _errorMessage.value = error.message }
            .isSuccess
    }

    fun validatePlanningRequest(
        satelliteId: String,
        codeStation: String,
        dureeSecondes: Int
    ) {
        viewModelScope.launch {
            repository.planifierFenetre(
                satelliteId = satelliteId,
                codeStation = codeStation,
                dureeSecondes = dureeSecondes
            ).onSuccess {
                _planningValidationMessage.value =
                    "Fenêtre planifiable pour $satelliteId depuis $codeStation."
                _errorMessage.value = null
            }.onFailure { error ->
                _planningValidationMessage.value = null
                _errorMessage.value = error.message
            }
        }
    }

    fun loadSatelliteDetail(satelliteId: String) {
        viewModelScope.launch {
            _selectedSatellite.value =
                _satellites.value.firstOrNull { satellite ->
                    satellite.idSatellite == satelliteId
                } ?: repository.getSatelliteById(satelliteId)
            _selectedSatelliteInstruments.value =
                repository.getSatelliteInstruments(satelliteId)
            _selectedSatelliteMissions.value =
                repository.getSatelliteMissions(satelliteId)
        }
    }

    fun getOrbite(idOrbite: Int): Orbite? {
        return _orbites.value.firstOrNull { orbite -> orbite.idOrbite == idOrbite }
    }

    fun estimateRemainingLifetimeMonths(satellite: Satellite): Int {
        val monthsElapsed = Period.between(satellite.dateLancement, LocalDate.now()).toTotalMonths()
        return max(0, satellite.dureeViePrevueMois - monthsElapsed.toInt())
    }

    fun clearErrorMessage() {
        _errorMessage.update { null }
    }

    fun clearPlanningValidationMessage() {
        _planningValidationMessage.update { null }
    }

    private fun formatCacheAgeLabel(lastUpdatedEpochMillis: Long?): String? {
        if (lastUpdatedEpochMillis == null) return null

        val updatedAt = Instant.ofEpochMilli(lastUpdatedEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val minutes = java.time.Duration.between(updatedAt, java.time.LocalDateTime.now())
            .toMinutes()
            .coerceAtLeast(0)

        return when {
            minutes < 1 -> "Mis à jour à l'instant"
            minutes < 60 -> "Mis à jour il y a $minutes min"
            else -> "Mis à jour il y a ${minutes / 60} h"
        }
    }

    private fun observeFavoriteSatellites() {
        viewModelScope.launch {
            repository.favoriteSatelliteIds.collect { favorites ->
                _favoriteSatelliteIds.value = favorites
            }
        }
    }
}
