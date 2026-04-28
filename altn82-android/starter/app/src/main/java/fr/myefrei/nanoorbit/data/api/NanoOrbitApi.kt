package fr.myefrei.nanoorbit.data.api

import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.MissionStats
import fr.myefrei.nanoorbit.data.models.Orbite
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.SatelliteInstrument
import fr.myefrei.nanoorbit.data.models.SatelliteMissionAssignment
import fr.myefrei.nanoorbit.data.models.SatelliteOperationnelSummary
import fr.myefrei.nanoorbit.data.models.StationSol
import fr.myefrei.nanoorbit.data.models.VolumeMensuel
import java.time.LocalDateTime
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NanoOrbitApi {
    @GET("orbites")
    suspend fun getOrbites(): List<Orbite>

    @GET("stations")
    suspend fun getStations(): List<StationSol>

    @GET("satellites")
    suspend fun getSatellites(): List<Satellite>

    @GET("satellites/operationnels")
    suspend fun getSatellitesOperationnels(): List<SatelliteOperationnelSummary>

    @GET("satellites/{id}")
    suspend fun getSatellite(
        @Path("id") satelliteId: String
    ): Satellite

    @GET("satellites/{id}/instruments")
    suspend fun getSatelliteInstruments(
        @Path("id") satelliteId: String
    ): List<SatelliteInstrument>

    @GET("satellites/{id}/missions")
    suspend fun getSatelliteMissions(
        @Path("id") satelliteId: String
    ): List<SatelliteMissionAssignment>

    @GET("fenetres")
    suspend fun getFenetres(): List<FenetreCom>

    @GET("missions/stats")
    suspend fun getMissionStats(): List<MissionStats>

    @GET("volumes/mensuels")
    suspend fun getVolumesMensuels(): List<VolumeMensuel>

    @POST("fenetres/validate")
    suspend fun validateFenetre(
        @Body request: FenetreValidationRequest
    ): FenetreValidationResponse

    @POST("fenetres")
    suspend fun createFenetre(
        @Body request: CreateFenetreRequest
    ): FenetreCom
}

data class FenetreValidationRequest(
    val satelliteId: String,
    val codeStation: String,
    val dureeSecondes: Int
)

data class FenetreValidationResponse(
    val isValid: Boolean,
    val message: String
)

data class CreateFenetreRequest(
    val satelliteId: String,
    val codeStation: String,
    val datetimeDebut: LocalDateTime,
    val dureeSecondes: Int,
    val elevationMaxDegres: Double
)
