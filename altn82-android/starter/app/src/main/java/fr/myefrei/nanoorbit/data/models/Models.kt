package fr.myefrei.nanoorbit.data.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Enum miroir de SATELLITE.statut et des CHECK Oracle chk_satellite_statut/chk_hist_*_statut.
 *
 * Q2: une enum class évite les valeurs libres ("operationel", "OP", etc.), garantit le respect
 * exact du CHECK Oracle et permet au compilateur d'imposer le traitement de tous les cas UI.
 */
enum class StatutSatellite(val libelleOracle: String) {
    @SerializedName("Opérationnel")
    OPERATIONNEL("Opérationnel"),
    @SerializedName("En veille")
    EN_VEILLE("En veille"),
    @SerializedName("Défaillant")
    DEFAILLANT("Défaillant"),
    @SerializedName("Désorbité")
    DESORBITE("Désorbité")
}

/**
 * Enum miroir de SATELLITE.format_cubesat et du CHECK Oracle chk_satellite_format.
 */
enum class FormatCubeSat(val libelleOracle: String) {
    @SerializedName("1U")
    U1("1U"),
    @SerializedName("3U")
    U3("3U"),
    @SerializedName("6U")
    U6("6U"),
    @SerializedName("12U")
    U12("12U")
}

/**
 * Enum miroir de ORBITE.type_orbite et du CHECK Oracle chk_orbite_type.
 */
enum class TypeOrbite(val libelleOracle: String) {
    @SerializedName("LEO")
    LEO("LEO"),
    @SerializedName("MEO")
    MEO("MEO"),
    @SerializedName("SSO")
    SSO("SSO"),
    @SerializedName("GEO")
    GEO("GEO")
}

/**
 * Enum miroir de FENETRE_COM.statut et du CHECK Oracle chk_fenetre_statut.
 */
enum class StatutFenetre(val libelleOracle: String) {
    @SerializedName("Planifiée")
    PLANIFIEE("Planifiée"),
    @SerializedName("Réalisée")
    REALISEE("Réalisée"),
    @SerializedName("Annulée")
    ANNULEE("Annulée")
}

/**
 * Enum miroir de STATION_SOL.statut et du CHECK Oracle chk_station_statut.
 */
enum class StatutStation(val libelleOracle: String) {
    @SerializedName("Active")
    ACTIVE("Active"),
    @SerializedName("Maintenance")
    MAINTENANCE("Maintenance"),
    @SerializedName("Inactive")
    INACTIVE("Inactive")
}

/**
 * Enum miroir de MISSION.statut_mission et du CHECK Oracle chk_mission_statut.
 */
enum class StatutMission(val libelleOracle: String) {
    @SerializedName("Active")
    ACTIVE("Active"),
    @SerializedName("Terminée")
    TERMINEE("Terminée")
}

/**
 * Enum miroir de EMBARQUEMENT.etat_fonctionnement et du CHECK Oracle chk_emb_etat.
 */
enum class EtatFonctionnementInstrument(val libelleOracle: String) {
    @SerializedName("Nominal")
    NOMINAL("Nominal"),
    @SerializedName("Dégradé")
    DEGRADE("Dégradé"),
    @SerializedName("Hors service")
    HORS_SERVICE("Hors service")
}

/**
 * Table ORBITE
 * - id_orbite -> idOrbite
 * - type_orbite -> typeOrbite
 * - altitude -> altitudeKm
 * - inclinaison -> inclinaisonDegres
 * - periode_orbitale -> periodeOrbitaleMinutes
 * - excentricite -> excentricite
 * - zone_couverture -> zoneCouverture
 */
data class Orbite(
    val idOrbite: Int,
    val typeOrbite: TypeOrbite,
    val altitudeKm: Int,
    val inclinaisonDegres: Double,
    val periodeOrbitaleMinutes: Double,
    val excentricite: Double,
    val zoneCouverture: String
)

/**
 * Table SATELLITE
 * - id_satellite -> idSatellite
 * - nom_satellite -> nomSatellite
 * - date_lancement -> dateLancement
 * - masse -> masseKg
 * - format_cubesat -> formatCubesat
 * - statut -> statut
 * - duree_vie_prevue -> dureeViePrevueMois
 * - capacite_batterie -> capaciteBatterieWh
 * - id_orbite -> idOrbite
 */
data class Satellite(
    val idSatellite: String,
    val nomSatellite: String,
    val dateLancement: LocalDate,
    val masseKg: Double,
    val formatCubesat: FormatCubeSat,
    val statut: StatutSatellite,
    val dureeViePrevueMois: Int,
    val capaciteBatterieWh: Double,
    val idOrbite: Int
)

/**
 * Vue V_SATELLITES_OPERATIONNELS
 * - nom_orbite -> nomOrbite
 * - nb_instruments_embarques -> nbInstrumentsEmbarques
 * - statut_batterie -> statutBatterie
 */
data class SatelliteOperationnelSummary(
    val idSatellite: String,
    val nomSatellite: String,
    val nomOrbite: String,
    val nbInstrumentsEmbarques: Int,
    val statutBatterie: String,
    val capaciteBatterieWh: Double
)

/**
 * Table INSTRUMENT
 * - ref_instrument -> refInstrument
 * - type_instrument -> typeInstrument
 * - modele -> modele
 * - resolution -> resolution
 * - consommation -> consommationW
 * - masse -> masseKg
 */
data class Instrument(
    val refInstrument: String,
    val typeInstrument: String,
    val modele: String,
    val resolution: Double?,
    val consommationW: Double,
    val masseKg: Double
)

/**
 * Table FENETRE_COM
 * - id_fenetre -> idFenetre
 * - datetime_debut -> datetimeDebut
 * - duree -> dureeSecondes
 * - elevation_max -> elevationMaxDegres
 * - volume_donnees -> volumeDonneesMb
 * - statut -> statut
 * - id_satellite -> idSatellite
 * - code_station -> codeStation
 *
 * Colonnes enrichies issues de V_FENETRES_DETAIL
 * - nom_satellite -> nomSatellite
 * - nom_station -> nomStation
 * - id_centre -> idCentre
 * - nom_centre -> nomCentre
 * - debut_formate -> debutFormate
 * - duree_formatee -> dureeFormatee
 */
data class FenetreCom(
    val idFenetre: Int,
    val datetimeDebut: LocalDateTime,
    val debutFormate: String? = null,
    val dureeSecondes: Int,
    val dureeFormatee: String? = null,
    val elevationMaxDegres: Double,
    val volumeDonneesMb: Double?,
    val statut: StatutFenetre,
    val idSatellite: String,
    val nomSatellite: String? = null,
    val codeStation: String,
    val nomStation: String? = null,
    val idCentre: Int? = null,
    val nomCentre: String? = null
)

/**
 * Table STATION_SOL
 * - code_station -> codeStation
 * - nom_station -> nomStation
 * - latitude -> latitude
 * - longitude -> longitude
 * - diametre_antenne -> diametreAntenneM
 * - bande_frequence -> bandeFrequence
 * - debit_max -> debitMaxMbps
 * - statut -> statut
 */
data class StationSol(
    val codeStation: String,
    val nomStation: String,
    val latitude: Double,
    val longitude: Double,
    val diametreAntenneM: Double,
    val bandeFrequence: String,
    val debitMaxMbps: Double,
    val statut: StatutStation
)

/**
 * Table MISSION
 * - id_mission -> idMission
 * - nom_mission -> nomMission
 * - objectif -> objectif
 * - zone_geo_cible -> zoneGeoCible
 * - date_debut -> dateDebut
 * - date_fin -> dateFin
 * - statut_mission -> statutMission
 */
data class Mission(
    val idMission: String,
    val nomMission: String,
    val objectif: String,
    val zoneGeoCible: String,
    val dateDebut: LocalDate,
    val dateFin: LocalDate?,
    val statutMission: StatutMission
)

/**
 * Vue V_STATS_MISSIONS
 */
data class MissionStats(
    val idMission: String,
    val nomMission: String,
    val statutMission: StatutMission,
    val nbSatellites: Int,
    val typesOrbitesRepresentes: String,
    val volumeTotalTelechargeMb: Double
)

/**
 * Vue materialisee MV_VOLUMES_MENSUELS
 */
data class VolumeMensuel(
    val moisReference: LocalDate,
    val idCentre: Int?,
    val nomCentre: String?,
    val typeSatellite: FormatCubeSat,
    val nbFenetresRealisees: Int,
    val volumeTotalMb: Double
)

/**
 * Table EMBARQUEMENT
 * - id_satellite -> idSatellite
 * - ref_instrument -> refInstrument
 * - date_integration -> dateIntegration
 * - etat_fonctionnement -> etatFonctionnement
 */
data class EmbarquementInstrument(
    val idSatellite: String,
    val refInstrument: String,
    val dateIntegration: LocalDate,
    val etatFonctionnement: EtatFonctionnementInstrument
)

/**
 * Table PARTICIPATION
 * - id_satellite -> idSatellite
 * - id_mission -> idMission
 * - role_satellite -> roleSatellite
 */
data class ParticipationMission(
    val idSatellite: String,
    val idMission: String,
    val roleSatellite: String
)

data class SatelliteInstrument(
    val instrument: Instrument,
    val etatFonctionnement: EtatFonctionnementInstrument
)

data class SatelliteMissionAssignment(
    val mission: Mission,
    val roleSatellite: String
)
