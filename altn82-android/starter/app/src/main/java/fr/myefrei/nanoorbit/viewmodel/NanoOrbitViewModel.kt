package fr.myefrei.nanoorbit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.myefrei.nanoorbit.data.mock.MockData
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.StatutSatellite
import fr.myefrei.nanoorbit.data.repository.NanoOrbitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NanoOrbitViewModel(
    private val repository: NanoOrbitRepository = NanoOrbitRepository()
) : ViewModel() {
    private val _satellites = MutableStateFlow<List<Satellite>>(emptyList())
    val satellites: StateFlow<List<Satellite>> = _satellites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatut = MutableStateFlow<StatutSatellite?>(null)
    val selectedStatut: StateFlow<StatutSatellite?> = _selectedStatut.asStateFlow()

    val filteredSatellites: StateFlow<List<Satellite>> = combine(
        _satellites,
        _searchQuery,
        _selectedStatut
    ) { satellites, query, statut ->
        satellites.filter { satellite ->
            val orbite = MockData.orbitesById[satellite.idOrbite]
            val matchesQuery = query.isBlank() ||
                satellite.nomSatellite.contains(query, ignoreCase = true) ||
                satellite.idSatellite.contains(query, ignoreCase = true) ||
                satellite.formatCubesat.libelleOracle.contains(query, ignoreCase = true) ||
                orbite?.typeOrbite?.libelleOracle?.contains(query, ignoreCase = true) == true
            val matchesStatut = statut == null || satellite.statut == statut

            matchesQuery && matchesStatut
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        loadSatellites()
    }

    fun loadSatellites() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                _satellites.value = repository.getSatellites()
            } catch (exception: Exception) {
                _errorMessage.value = exception.message
                    ?: "Impossible de charger les satellites NanoOrbit."
            } finally {
                _isLoading.value = false
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
        loadSatellites()
    }

    fun validateFenetreDuration(dureeSecondes: Int): Boolean {
        return repository.validateFenetreDuration(dureeSecondes)
            .onFailure { error -> _errorMessage.value = error.message }
            .isSuccess
    }

    fun clearErrorMessage() {
        _errorMessage.update { null }
    }
}
