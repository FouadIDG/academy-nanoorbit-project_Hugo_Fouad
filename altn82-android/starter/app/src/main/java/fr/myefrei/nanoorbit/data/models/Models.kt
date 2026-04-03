package fr.myefrei.nanoorbit.data.models

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Enum miroir de SATELLITE.statut et des CHECK Oracle chk_satellite_statut/chk_hist_*_statut.
 *
 * Q2: une enum class évite les valeurs libres ("operationel", "OP", etc.), garantit le respect
 * exact du CHECK Oracle et permet au compilateur d'imposer le traitement de tous les cas UI.
 */
enum class StatutSatellite(val libelleOracle: String) {
    OPERATIONNEL("Opérationnel"),
    EN_VEILLE("En veille"),
    DEFAILLANT("Défaillant"),
    DESORBITE("Désorbité")
}

/**
 * Enum miroir de SATELLITE.format_cubesat et du CHECK Oracle chk_satellite_format.
 */
enum class FormatCubeSat(val libelleOracle: String) {
    U1("1U"),
    U3("3U"),
    U6("6U"),
    U12("12U")
}

/**
 * Enum miroir de ORBITE.type_orbite et du CHECK Oracle chk_orbite_type.
 */
enum class TypeOrbite(val libelleOracle: String) {
    LEO("LEO"),
    MEO("MEO"),
    SSO("SSO"),
    GEO("GEO")
}

/**
 * Enum miroir de FENETRE_COM.statut et du CHECK Oracle chk_fenetre_statut.
 */
enum class StatutFenetre(val libelleOracle: String) {
    PLANIFIEE("Planifiée"),
    REALISEE("Réalisée"),
    ANNULEE("Annulée")
}

/**
 * Enum miroir de STATION_SOL.statut et du CHECK Oracle chk_station_statut.
 */
enum class StatutStation(val libelleOracle: String) {
    ACTIVE("Active"),
    MAINTENANCE("Maintenance"),
    INACTIVE("Inactive")
}

/**
 * Enum miroir de MISSION.statut_mission et du CHECK Oracle chk_mission_statut.
 */
enum class StatutMission(val libelleOracle: String) {
    ACTIVE("Active"),
    TERMINEE("Terminée")
}

/**
 * Enum miroir de EMBARQUEMENT.etat_fonctionnement et du CHECK Oracle chk_emb_etat.
 */
enum class EtatFonctionnementInstrument(val libelleOracle: String) {
    NOMINAL("Nominal"),
    DEGRADE("Dégradé"),
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
 */
data class FenetreCom(
    val idFenetre: Int,
    val datetimeDebut: LocalDateTime,
    val dureeSecondes: Int,
    val elevationMaxDegres: Double,
    val volumeDonneesMb: Double?,
    val statut: StatutFenetre,
    val idSatellite: String,
    val codeStation: String
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
