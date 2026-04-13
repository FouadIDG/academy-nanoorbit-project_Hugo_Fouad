package fr.myefrei.nanoorbit.data.repository

import fr.myefrei.nanoorbit.data.api.NanoOrbitApi
import fr.myefrei.nanoorbit.data.api.FenetreValidationRequest
import fr.myefrei.nanoorbit.data.local.NanoOrbitDao
import fr.myefrei.nanoorbit.data.local.toEntity
import fr.myefrei.nanoorbit.data.local.toModel
import fr.myefrei.nanoorbit.data.mock.MockData
import fr.myefrei.nanoorbit.data.models.Orbite
import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.SatelliteInstrument
import fr.myefrei.nanoorbit.data.models.SatelliteMissionAssignment
import fr.myefrei.nanoorbit.data.models.StatutSatellite
import fr.myefrei.nanoorbit.data.models.StationSol
import fr.myefrei.nanoorbit.data.preferences.FavoritesPreferences
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class NanoOrbitRepository(
    private val dao: NanoOrbitDao? = null,
    private val favoritesPreferences: FavoritesPreferences? = null,
    private val api: NanoOrbitApi? = null
) {
    data class CacheFirstResult<T>(
        val data: T,
        val fromCache: Boolean,
        val fromMock: Boolean,
        val lastUpdatedEpochMillis: Long?
    )

    val favoriteSatelliteIds: Flow<Set<String>> =
        favoritesPreferences?.favoriteSatelliteIds ?: flowOf(emptySet())

    suspend fun getOrbites(): List<Orbite> {
        delay(NETWORK_LATENCY_MS)
        api?.let { remoteApi ->
            try {
                return remoteApi.getOrbites()
            } catch (error: Exception) {
                if (!isRemoteUnavailable(error)) throw error
            }
        }

        return MockData.orbites
    }

    suspend fun getStationsSol(): List<StationSol> {
        delay(NETWORK_LATENCY_MS)
        api?.let { remoteApi ->
            try {
                return remoteApi.getStations()
            } catch (error: Exception) {
                if (!isRemoteUnavailable(error)) throw error
            }
        }

        return MockData.stationsSol
    }

    suspend fun getSatellites(): CacheFirstResult<List<Satellite>> {
        val cachedSatellites = withContext(Dispatchers.IO) {
            dao?.getSatelliteEntities().orEmpty()
        }
        val cachedUpdatedAt = withContext(Dispatchers.IO) {
            dao?.getSatellitesLastUpdatedAt()
        }

        if (api == null) {
            return if (cachedSatellites.isNotEmpty()) {
                CacheFirstResult(
                    data = cachedSatellites.map { satelliteEntity -> satelliteEntity.toModel() },
                    fromCache = true,
                    fromMock = false,
                    lastUpdatedEpochMillis = cachedUpdatedAt
                )
            } else {
                CacheFirstResult(
                    data = MockData.satellites,
                    fromCache = false,
                    fromMock = true,
                    lastUpdatedEpochMillis = null
                )
            }
        }

        return try {
            val networkSatellites = fetchSatellitesFromRemote()
            persistSatellites(networkSatellites)

            CacheFirstResult(
                data = networkSatellites,
                fromCache = false,
                fromMock = false,
                lastUpdatedEpochMillis = Instant.now().toEpochMilli()
            )
        } catch (error: Exception) {
            if (!isRemoteUnavailable(error)) throw error

            if (cachedSatellites.isNotEmpty()) {
                CacheFirstResult(
                    data = cachedSatellites.map { satelliteEntity -> satelliteEntity.toModel() },
                    fromCache = true,
                    fromMock = false,
                    lastUpdatedEpochMillis = cachedUpdatedAt
                )
            } else {
                CacheFirstResult(
                    data = MockData.satellites,
                    fromCache = false,
                    fromMock = true,
                    lastUpdatedEpochMillis = null
                )
            }
        }
    }

    suspend fun getSatelliteInstruments(satelliteId: String): List<SatelliteInstrument> {
        delay(NETWORK_LATENCY_MS)

        api?.let { remoteApi ->
            try {
                return remoteApi.getSatelliteInstruments(satelliteId)
            } catch (error: Exception) {
                if (!isRemoteUnavailable(error)) throw error
            }
        }

        return MockData.embarquements
            .filter { embarquement -> embarquement.idSatellite == satelliteId }
            .mapNotNull { embarquement ->
                MockData.instrumentsByRef[embarquement.refInstrument]?.let { instrument ->
                    SatelliteInstrument(
                        instrument = instrument,
                        etatFonctionnement = embarquement.etatFonctionnement
                    )
                }
            }
    }

    suspend fun getSatelliteMissions(satelliteId: String): List<SatelliteMissionAssignment> {
        delay(NETWORK_LATENCY_MS)

        api?.let { remoteApi ->
            try {
                return remoteApi.getSatelliteMissions(satelliteId)
            } catch (error: Exception) {
                if (!isRemoteUnavailable(error)) throw error
            }
        }

        return MockData.participations
            .filter { participation -> participation.idSatellite == satelliteId }
            .mapNotNull { participation ->
                MockData.missions
                    .firstOrNull { mission -> mission.idMission == participation.idMission }
                    ?.let { mission ->
                        SatelliteMissionAssignment(
                            mission = mission,
                            roleSatellite = participation.roleSatellite
                        )
                    }
            }
    }

    suspend fun getSatelliteById(satelliteId: String): Satellite? {
        delay(NETWORK_LATENCY_MS)

        api?.let { remoteApi ->
            return try {
                remoteApi.getSatellite(satelliteId)
            } catch (error: HttpException) {
                when {
                    error.code() == 404 -> null
                    isRemoteUnavailable(error) -> MockData.satellites.firstOrNull { satellite ->
                        satellite.idSatellite == satelliteId
                    }
                    else -> throw error
                }
            } catch (error: IOException) {
                MockData.satellites.firstOrNull { satellite ->
                    satellite.idSatellite == satelliteId
                }
            }
        }

        return MockData.satellites.firstOrNull { satellite ->
            satellite.idSatellite == satelliteId
        }
    }

    suspend fun planifierFenetre(
        satelliteId: String,
        codeStation: String,
        dureeSecondes: Int
    ): Result<Unit> {
        api?.let { remoteApi ->
            return try {
                val response = remoteApi.validateFenetre(
                    FenetreValidationRequest(
                        satelliteId = satelliteId,
                        codeStation = codeStation,
                        dureeSecondes = dureeSecondes
                    )
                )
                if (response.isValid) {
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalArgumentException(response.message))
                }
            } catch (error: HttpException) {
                if (isRemoteUnavailable(error)) {
                    validateFenetreLocally(satelliteId, codeStation, dureeSecondes)
                } else {
                    Result.failure(
                        IllegalArgumentException(
                            parseApiErrorMessage(
                                error.response()?.errorBody()?.string()
                            )
                                ?: error.message()
                        )
                    )
                }
            } catch (error: IOException) {
                validateFenetreLocally(satelliteId, codeStation, dureeSecondes)
            }
        }

        return validateFenetreLocally(satelliteId, codeStation, dureeSecondes)
    }

    private fun validateFenetreLocally(
        satelliteId: String,
        codeStation: String,
        dureeSecondes: Int
    ): Result<Unit> {
        val satellite = MockData.satellites.firstOrNull { item ->
            item.idSatellite == satelliteId
        } ?: return Result.failure(IllegalArgumentException("Satellite introuvable : $satelliteId"))

        if (satellite.statut == StatutSatellite.DESORBITE) {
            return Result.failure(
                IllegalArgumentException(
                    "Insertion refusée : le satellite est désorbité."
                )
            )
        }

        val station = MockData.stationsByCode[codeStation]
            ?: return Result.failure(IllegalArgumentException("Station introuvable : $codeStation"))

        if (station.statut != fr.myefrei.nanoorbit.data.models.StatutStation.ACTIVE) {
            return Result.failure(
                IllegalArgumentException("Insertion refusée : la station est en maintenance.")
            )
        }

        return validateFenetreDuration(dureeSecondes)
    }

    suspend fun getFenetres(): CacheFirstResult<List<FenetreCom>> {
        val cachedFenetres = withContext(Dispatchers.IO) {
            dao?.getFenetreEntities().orEmpty()
        }
        val cachedUpdatedAt = withContext(Dispatchers.IO) {
            dao?.getFenetresLastUpdatedAt()
        }

        if (api == null) {
            return if (cachedFenetres.isNotEmpty()) {
                CacheFirstResult(
                    data = cachedFenetres.map { fenetreEntity -> fenetreEntity.toModel() },
                    fromCache = true,
                    fromMock = false,
                    lastUpdatedEpochMillis = cachedUpdatedAt
                )
            } else {
                CacheFirstResult(
                    data = MockData.fenetresCom,
                    fromCache = false,
                    fromMock = true,
                    lastUpdatedEpochMillis = null
                )
            }
        }

        return try {
            val networkFenetres = fetchFenetresFromRemote()
            persistFenetres(networkFenetres)

            CacheFirstResult(
                data = networkFenetres,
                fromCache = false,
                fromMock = false,
                lastUpdatedEpochMillis = Instant.now().toEpochMilli()
            )
        } catch (error: Exception) {
            if (!isRemoteUnavailable(error)) throw error

            if (cachedFenetres.isNotEmpty()) {
                CacheFirstResult(
                    data = cachedFenetres.map { fenetreEntity -> fenetreEntity.toModel() },
                    fromCache = true,
                    fromMock = false,
                    lastUpdatedEpochMillis = cachedUpdatedAt
                )
            } else {
                CacheFirstResult(
                    data = MockData.fenetresCom,
                    fromCache = false,
                    fromMock = true,
                    lastUpdatedEpochMillis = null
                )
            }
        }
    }

    suspend fun toggleFavoriteSatellite(satelliteId: String) {
        favoritesPreferences?.toggleFavoriteSatellite(satelliteId)
    }

    /**
     * RG-F04 côté client : la durée d'une fenêtre doit rester dans [1, 900] secondes.
     * C'est le miroir applicatif du CHECK Oracle chk_fenetre_duree et de la validation PL/SQL.
     */
    fun validateFenetreDuration(dureeSecondes: Int): Result<Unit> {
        return if (dureeSecondes in MIN_WINDOW_DURATION_SECONDS..MAX_WINDOW_DURATION_SECONDS) {
            Result.success(Unit)
        } else {
            Result.failure(
                IllegalArgumentException(
                    "Durée invalide : elle doit être comprise entre 1 et 900 secondes."
                )
            )
        }
    }

    private suspend fun fetchSatellitesFromRemote(): List<Satellite> {
        delay(NETWORK_LATENCY_MS)
        return api?.getSatellites() ?: MockData.satellites
    }

    private suspend fun fetchFenetresFromRemote(): List<FenetreCom> {
        delay(NETWORK_LATENCY_MS)
        return api?.getFenetres() ?: MockData.fenetresCom
    }

    private suspend fun persistSatellites(satellites: List<Satellite>) {
        val now = Instant.now().toEpochMilli()
        withContext(Dispatchers.IO) {
            dao?.clearSatellites()
            dao?.upsertSatelliteEntities(
                satellites.map { satellite -> satellite.toEntity(lastUpdatedEpochMillis = now) }
            )
        }
    }

    /**
     * Cache-First pour le lien ALTN83 Q3 : si le serveur central est indisponible, l'app peut relire
     * les fenêtres stockées dans Room et continuer à présenter le planning déjà synchronisé.
     */
    private suspend fun persistFenetres(fenetres: List<FenetreCom>) {
        val now = Instant.now().toEpochMilli()
        withContext(Dispatchers.IO) {
            dao?.clearFenetres()
            dao?.upsertFenetreEntities(
                fenetres.map { fenetre -> fenetre.toEntity(lastUpdatedEpochMillis = now) }
            )
        }
    }

    private fun parseApiErrorMessage(rawBody: String?): String? {
        if (rawBody.isNullOrBlank()) return null

        return runCatching {
            JSONObject(rawBody).optString("message")
                .takeIf { message -> message.isNotBlank() }
        }.getOrNull() ?: rawBody
    }

    private fun isRemoteUnavailable(error: Throwable): Boolean {
        return error is IOException || (error is HttpException && error.code() >= 500)
    }

    private companion object {
        const val NETWORK_LATENCY_MS = 500L
        const val MIN_WINDOW_DURATION_SECONDS = 1
        const val MAX_WINDOW_DURATION_SECONDS = 900
    }
}
