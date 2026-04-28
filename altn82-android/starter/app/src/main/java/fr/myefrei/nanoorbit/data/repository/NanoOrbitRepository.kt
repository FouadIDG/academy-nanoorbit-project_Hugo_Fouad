package fr.myefrei.nanoorbit.data.repository

import fr.myefrei.nanoorbit.data.api.NanoOrbitApi
import fr.myefrei.nanoorbit.data.api.CreateFenetreRequest
import fr.myefrei.nanoorbit.data.local.NanoOrbitDao
import fr.myefrei.nanoorbit.data.local.PendingFenetreEntity
import fr.myefrei.nanoorbit.data.local.toEntity
import fr.myefrei.nanoorbit.data.local.toModel
import fr.myefrei.nanoorbit.data.mock.MockData
import fr.myefrei.nanoorbit.data.models.Orbite
import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.PendingFenetrePlanification
import fr.myefrei.nanoorbit.data.models.PendingSyncStatus
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.SatelliteInstrument
import fr.myefrei.nanoorbit.data.models.SatelliteMissionAssignment
import fr.myefrei.nanoorbit.data.models.StatutSatellite
import fr.myefrei.nanoorbit.data.models.StatutStation
import fr.myefrei.nanoorbit.data.models.StationSol
import fr.myefrei.nanoorbit.data.preferences.FavoritesPreferences
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class NanoOrbitRepository(
    private val dao: NanoOrbitDao? = null,
    private val favoritesPreferences: FavoritesPreferences? = null,
    private val api: NanoOrbitApi? = null,
    private val schedulePendingSync: (() -> Unit)? = null
) {
    data class CacheFirstResult<T>(
        val data: T,
        val fromCache: Boolean,
        val fromMock: Boolean,
        val lastUpdatedEpochMillis: Long?
    )

    data class PlanificationResult(
        val fenetre: FenetreCom?,
        val queued: Boolean,
        val message: String
    )

    data class PendingSyncResult(
        val syncedCount: Int,
        val failedCount: Int,
        val hasTransientFailure: Boolean
    )

    val favoriteSatelliteIds: Flow<Set<String>> =
        favoritesPreferences?.favoriteSatelliteIds ?: flowOf(emptySet())

    val pendingFenetres: Flow<List<PendingFenetrePlanification>> =
        dao?.observePendingFenetreEntities()
            ?.map { entities -> entities.map { entity -> entity.toModel() } }
            ?: flowOf(emptyList())

    val cachedFenetres: Flow<List<FenetreCom>> =
        dao?.observeFenetreEntities()
            ?.map { entities -> entities.map { entity -> entity.toModel() } }
            ?: flowOf(emptyList())

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
        datetimeDebut: LocalDateTime,
        dureeSecondes: Int,
        elevationMaxDegres: Double
    ): Result<PlanificationResult> {
        val request = CreateFenetreRequest(
            satelliteId = satelliteId,
            codeStation = codeStation,
            datetimeDebut = datetimeDebut,
            dureeSecondes = dureeSecondes,
            elevationMaxDegres = elevationMaxDegres
        )

        validateFenetreLocally(
            request = request,
            checkOverlaps = false
        ).onFailure { error ->
            return Result.failure(error)
        }

        api?.let { remoteApi ->
            return try {
                val fenetre = remoteApi.createFenetre(request)
                persistSingleFenetre(fenetre)
                Result.success(
                    PlanificationResult(
                        fenetre = fenetre,
                        queued = false,
                        message = "Fenêtre planifiée pour $satelliteId depuis $codeStation."
                    )
                )
            } catch (error: HttpException) {
                if (isRemoteUnavailable(error)) {
                    queueFenetre(request)
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
                queueFenetre(request)
            }
        }

        return queueFenetre(request)
    }

    private suspend fun queueFenetre(
        request: CreateFenetreRequest
    ): Result<PlanificationResult> {
        validateFenetreLocally(
            request = request,
            checkOverlaps = true
        ).onFailure { error ->
            return Result.failure(error)
        }

        val localId = withContext(Dispatchers.IO) {
            dao?.insertPendingFenetreEntity(
                PendingFenetreEntity(
                    satelliteId = request.satelliteId,
                    codeStation = request.codeStation,
                    datetimeDebutIso = request.datetimeDebut.toString(),
                    dureeSecondes = request.dureeSecondes,
                    elevationMaxDegres = request.elevationMaxDegres,
                    status = PendingSyncStatus.PENDING.name,
                    createdAtEpochMillis = Instant.now().toEpochMilli(),
                    lastError = null,
                    retryCount = 0
                )
            )
        }

        if (localId == null) {
            return Result.failure(
                IllegalStateException(
                    "Impossible de mettre la fenêtre en attente : cache local indisponible."
                )
            )
        }

        schedulePendingSync?.invoke()

        return Result.success(
            PlanificationResult(
                fenetre = null,
                queued = true,
                message = "API indisponible : fenêtre mise en file d'attente."
            )
        )
    }

    private suspend fun validateFenetreLocally(
        request: CreateFenetreRequest,
        checkOverlaps: Boolean
    ): Result<Unit> {
        validateFenetreDuration(request.dureeSecondes).onFailure { error ->
            return Result.failure(error)
        }

        if (request.elevationMaxDegres !in 0.0..90.0) {
            return Result.failure(
                IllegalArgumentException(
                    "Élévation invalide : elle doit être comprise entre 0 et 90 degrés."
                )
            )
        }

        val satellites = withContext(Dispatchers.IO) {
            dao?.getSatelliteEntities()
                ?.map { satelliteEntity -> satelliteEntity.toModel() }
                .orEmpty()
        }.ifEmpty { MockData.satellites }

        val satellite = satellites.firstOrNull { item ->
            item.idSatellite == request.satelliteId
        } ?: return Result.failure(
            IllegalArgumentException("Satellite introuvable : ${request.satelliteId}")
        )

        if (satellite.statut == StatutSatellite.DESORBITE) {
            return Result.failure(
                IllegalArgumentException(
                    "Insertion refusée : le satellite est désorbité."
                )
            )
        }

        val station = MockData.stationsByCode[request.codeStation]
            ?: return Result.failure(
                IllegalArgumentException("Station introuvable : ${request.codeStation}")
            )

        if (station.statut != StatutStation.ACTIVE) {
            return Result.failure(
                IllegalArgumentException("Insertion refusée : la station est en maintenance.")
            )
        }

        if (checkOverlaps) {
            validateLocalOverlaps(request).onFailure { error ->
                return Result.failure(error)
            }
        }

        return Result.success(Unit)
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

    suspend fun syncPendingFenetres(): PendingSyncResult {
        val localDao = dao ?: return PendingSyncResult(
            syncedCount = 0,
            failedCount = 0,
            hasTransientFailure = false
        )
        val remoteApi = api ?: return PendingSyncResult(
            syncedCount = 0,
            failedCount = 0,
            hasTransientFailure = true
        )

        val pending = withContext(Dispatchers.IO) {
            localDao.getPendingFenetreSyncCandidates()
        }
        var syncedCount = 0
        var failedCount = 0
        var hasTransientFailure = false

        pending.forEach { entity ->
            val request = CreateFenetreRequest(
                satelliteId = entity.satelliteId,
                codeStation = entity.codeStation,
                datetimeDebut = LocalDateTime.parse(entity.datetimeDebutIso),
                dureeSecondes = entity.dureeSecondes,
                elevationMaxDegres = entity.elevationMaxDegres
            )

            try {
                val fenetre = remoteApi.createFenetre(request)
                persistSingleFenetre(fenetre)
                withContext(Dispatchers.IO) {
                    localDao.deletePendingFenetreEntity(entity.localId)
                }
                syncedCount += 1
            } catch (error: HttpException) {
                val message = parseApiErrorMessage(error.response()?.errorBody()?.string())
                    ?: error.message()
                if (error.code() in 400..499) {
                    failedCount += 1
                    withContext(Dispatchers.IO) {
                        localDao.updatePendingFenetreStatus(
                            localId = entity.localId,
                            status = PendingSyncStatus.FAILED.name,
                            lastError = message
                        )
                    }
                } else {
                    hasTransientFailure = true
                    withContext(Dispatchers.IO) {
                        localDao.updatePendingFenetreStatus(
                            localId = entity.localId,
                            status = PendingSyncStatus.PENDING.name,
                            lastError = message
                        )
                    }
                }
            } catch (error: IOException) {
                hasTransientFailure = true
                withContext(Dispatchers.IO) {
                    localDao.updatePendingFenetreStatus(
                        localId = entity.localId,
                        status = PendingSyncStatus.PENDING.name,
                        lastError = error.message ?: "API indisponible."
                    )
                }
            }
        }

        return PendingSyncResult(
            syncedCount = syncedCount,
            failedCount = failedCount,
            hasTransientFailure = hasTransientFailure
        )
    }

    private suspend fun validateLocalOverlaps(
        request: CreateFenetreRequest
    ): Result<Unit> {
        val cachedFenetres = withContext(Dispatchers.IO) {
            dao?.getFenetreEntities()
                ?.map { fenetreEntity -> fenetreEntity.toModel() }
                .orEmpty()
        }.ifEmpty { MockData.fenetresCom }

        val pendingFenetres = withContext(Dispatchers.IO) {
            dao?.getPendingFenetreSyncCandidates().orEmpty()
        }

        val hasSatelliteOverlap = cachedFenetres.any { fenetre ->
            fenetre.idSatellite == request.satelliteId &&
                overlaps(
                    startA = request.datetimeDebut,
                    durationASeconds = request.dureeSecondes,
                    startB = fenetre.datetimeDebut,
                    durationBSeconds = fenetre.dureeSecondes
                )
        } || pendingFenetres.any { pending ->
            pending.satelliteId == request.satelliteId &&
                overlaps(
                    startA = request.datetimeDebut,
                    durationASeconds = request.dureeSecondes,
                    startB = LocalDateTime.parse(pending.datetimeDebutIso),
                    durationBSeconds = pending.dureeSecondes
                )
        }

        if (hasSatelliteOverlap) {
            return Result.failure(
                IllegalArgumentException(
                    "Chevauchement temporel détecté pour le satellite."
                )
            )
        }

        val hasStationOverlap = cachedFenetres.any { fenetre ->
            fenetre.codeStation == request.codeStation &&
                overlaps(
                    startA = request.datetimeDebut,
                    durationASeconds = request.dureeSecondes,
                    startB = fenetre.datetimeDebut,
                    durationBSeconds = fenetre.dureeSecondes
                )
        } || pendingFenetres.any { pending ->
            pending.codeStation == request.codeStation &&
                overlaps(
                    startA = request.datetimeDebut,
                    durationASeconds = request.dureeSecondes,
                    startB = LocalDateTime.parse(pending.datetimeDebutIso),
                    durationBSeconds = pending.dureeSecondes
                )
        }

        if (hasStationOverlap) {
            return Result.failure(
                IllegalArgumentException(
                    "Chevauchement temporel détecté pour la station."
                )
            )
        }

        return Result.success(Unit)
    }

    private fun overlaps(
        startA: LocalDateTime,
        durationASeconds: Int,
        startB: LocalDateTime,
        durationBSeconds: Int
    ): Boolean {
        val endA = startA.plusSeconds(durationASeconds.toLong())
        val endB = startB.plusSeconds(durationBSeconds.toLong())
        return startA.isBefore(endB) && endA.isAfter(startB)
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

    private suspend fun persistSingleFenetre(fenetre: FenetreCom) {
        val now = Instant.now().toEpochMilli()
        withContext(Dispatchers.IO) {
            dao?.upsertFenetreEntities(
                listOf(fenetre.toEntity(lastUpdatedEpochMillis = now))
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
