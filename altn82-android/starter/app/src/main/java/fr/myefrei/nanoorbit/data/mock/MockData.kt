package fr.myefrei.nanoorbit.data.mock

import fr.myefrei.nanoorbit.data.models.EmbarquementInstrument
import fr.myefrei.nanoorbit.data.models.EtatFonctionnementInstrument
import fr.myefrei.nanoorbit.data.models.FenetreCom
import fr.myefrei.nanoorbit.data.models.FormatCubeSat
import fr.myefrei.nanoorbit.data.models.Instrument
import fr.myefrei.nanoorbit.data.models.Mission
import fr.myefrei.nanoorbit.data.models.Orbite
import fr.myefrei.nanoorbit.data.models.Satellite
import fr.myefrei.nanoorbit.data.models.StatutFenetre
import fr.myefrei.nanoorbit.data.models.StatutMission
import fr.myefrei.nanoorbit.data.models.StatutSatellite
import fr.myefrei.nanoorbit.data.models.StatutStation
import fr.myefrei.nanoorbit.data.models.StationSol
import fr.myefrei.nanoorbit.data.models.TypeOrbite
import java.time.LocalDate
import java.time.LocalDateTime

object MockData {
    val orbites = listOf(
        Orbite(
            idOrbite = 1,
            typeOrbite = TypeOrbite.SSO,
            altitudeKm = 550,
            inclinaisonDegres = 97.6,
            periodeOrbitaleMinutes = 95.5,
            excentricite = 0.0010,
            zoneCouverture = "Polaire globale — Europe / Arctique"
        ),
        Orbite(
            idOrbite = 2,
            typeOrbite = TypeOrbite.SSO,
            altitudeKm = 700,
            inclinaisonDegres = 98.2,
            periodeOrbitaleMinutes = 98.8,
            excentricite = 0.0008,
            zoneCouverture = "Polaire globale — haute latitude"
        ),
        Orbite(
            idOrbite = 3,
            typeOrbite = TypeOrbite.LEO,
            altitudeKm = 400,
            inclinaisonDegres = 51.6,
            periodeOrbitaleMinutes = 92.6,
            excentricite = 0.0020,
            zoneCouverture = "Équatoriale — zone tropicale"
        )
    )

    val orbitesById = orbites.associateBy { it.idOrbite }

    val satellites = listOf(
        Satellite(
            idSatellite = "SAT-001",
            nomSatellite = "NanoOrbit-Alpha",
            dateLancement = LocalDate.of(2022, 3, 15),
            masseKg = 1.30,
            formatCubesat = FormatCubeSat.U3,
            statut = StatutSatellite.OPERATIONNEL,
            dureeViePrevueMois = 60,
            capaciteBatterieWh = 20.0,
            idOrbite = 1
        ),
        Satellite(
            idSatellite = "SAT-002",
            nomSatellite = "NanoOrbit-Beta",
            dateLancement = LocalDate.of(2022, 3, 15),
            masseKg = 1.30,
            formatCubesat = FormatCubeSat.U3,
            statut = StatutSatellite.OPERATIONNEL,
            dureeViePrevueMois = 60,
            capaciteBatterieWh = 20.0,
            idOrbite = 1
        ),
        Satellite(
            idSatellite = "SAT-003",
            nomSatellite = "NanoOrbit-Gamma",
            dateLancement = LocalDate.of(2023, 6, 10),
            masseKg = 2.00,
            formatCubesat = FormatCubeSat.U6,
            statut = StatutSatellite.OPERATIONNEL,
            dureeViePrevueMois = 84,
            capaciteBatterieWh = 40.0,
            idOrbite = 2
        ),
        Satellite(
            idSatellite = "SAT-004",
            nomSatellite = "NanoOrbit-Delta",
            dateLancement = LocalDate.of(2023, 6, 10),
            masseKg = 2.00,
            formatCubesat = FormatCubeSat.U6,
            statut = StatutSatellite.EN_VEILLE,
            dureeViePrevueMois = 84,
            capaciteBatterieWh = 40.0,
            idOrbite = 2
        ),
        Satellite(
            idSatellite = "SAT-005",
            nomSatellite = "NanoOrbit-Epsilon",
            dateLancement = LocalDate.of(2021, 11, 20),
            masseKg = 4.50,
            formatCubesat = FormatCubeSat.U12,
            statut = StatutSatellite.DESORBITE,
            dureeViePrevueMois = 36,
            capaciteBatterieWh = 80.0,
            idOrbite = 3
        )
    )

    val instruments = listOf(
        Instrument(
            refInstrument = "INS-CAM-01",
            typeInstrument = "Caméra optique",
            modele = "PlanetScope-Mini",
            resolution = 3.0,
            consommationW = 2.5,
            masseKg = 0.400
        ),
        Instrument(
            refInstrument = "INS-IR-01",
            typeInstrument = "Infrarouge",
            modele = "FLIR-Lepton-3",
            resolution = 160.0,
            consommationW = 1.2,
            masseKg = 0.150
        ),
        Instrument(
            refInstrument = "INS-AIS-01",
            typeInstrument = "Récepteur AIS",
            modele = "ShipTrack-V2",
            resolution = null,
            consommationW = 0.8,
            masseKg = 0.120
        ),
        Instrument(
            refInstrument = "INS-SPEC-01",
            typeInstrument = "Spectromètre",
            modele = "HyperSpec-Nano",
            resolution = 30.0,
            consommationW = 3.1,
            masseKg = 0.600
        )
    )

    val instrumentsByRef = instruments.associateBy { it.refInstrument }

    val stationsSol = listOf(
        StationSol(
            codeStation = "GS-TLS-01",
            nomStation = "Toulouse Ground Station",
            latitude = 43.604700,
            longitude = 1.444200,
            diametreAntenneM = 3.5,
            bandeFrequence = "S",
            debitMaxMbps = 150.0,
            statut = StatutStation.ACTIVE
        ),
        StationSol(
            codeStation = "GS-KIR-01",
            nomStation = "Kiruna Arctic Station",
            latitude = 67.855700,
            longitude = 20.225300,
            diametreAntenneM = 5.4,
            bandeFrequence = "X",
            debitMaxMbps = 400.0,
            statut = StatutStation.ACTIVE
        ),
        StationSol(
            codeStation = "GS-SGP-01",
            nomStation = "Singapore Station",
            latitude = 1.352100,
            longitude = 103.819800,
            diametreAntenneM = 3.0,
            bandeFrequence = "S",
            debitMaxMbps = 120.0,
            statut = StatutStation.MAINTENANCE
        )
    )

    val stationsByCode = stationsSol.associateBy { it.codeStation }

    val missions = listOf(
        Mission(
            idMission = "MSN-ARC-2023",
            nomMission = "ArcticWatch 2023",
            objectif = "Surveillance fonte des glaces et dynamique des banquises",
            zoneGeoCible = "Arctique / Groenland",
            dateDebut = LocalDate.of(2023, 1, 1),
            dateFin = null,
            statutMission = StatutMission.ACTIVE
        ),
        Mission(
            idMission = "MSN-DEF-2022",
            nomMission = "DeforestAlert",
            objectif = "Détection et cartographie de la déforestation en temps quasi-réel",
            zoneGeoCible = "Amazonie / Congo",
            dateDebut = LocalDate.of(2022, 6, 1),
            dateFin = LocalDate.of(2023, 5, 31),
            statutMission = StatutMission.TERMINEE
        ),
        Mission(
            idMission = "MSN-COAST-2024",
            nomMission = "CoastGuard 2024",
            objectif = "Surveillance évolution du trait de côte et détection d'érosion",
            zoneGeoCible = "Méditerranée / Atlantique",
            dateDebut = LocalDate.of(2024, 3, 1),
            dateFin = null,
            statutMission = StatutMission.ACTIVE
        )
    )

    val fenetresCom = listOf(
        FenetreCom(
            idFenetre = 1,
            datetimeDebut = LocalDateTime.of(2024, 1, 15, 9, 14),
            dureeSecondes = 420,
            elevationMaxDegres = 82.3,
            volumeDonneesMb = 1250.0,
            statut = StatutFenetre.REALISEE,
            idSatellite = "SAT-001",
            codeStation = "GS-KIR-01"
        ),
        FenetreCom(
            idFenetre = 2,
            datetimeDebut = LocalDateTime.of(2024, 1, 15, 11, 52),
            dureeSecondes = 310,
            elevationMaxDegres = 67.1,
            volumeDonneesMb = 890.0,
            statut = StatutFenetre.REALISEE,
            idSatellite = "SAT-002",
            codeStation = "GS-TLS-01"
        ),
        FenetreCom(
            idFenetre = 3,
            datetimeDebut = LocalDateTime.of(2024, 1, 16, 8, 30),
            dureeSecondes = 540,
            elevationMaxDegres = 88.9,
            volumeDonneesMb = 1680.0,
            statut = StatutFenetre.REALISEE,
            idSatellite = "SAT-003",
            codeStation = "GS-KIR-01"
        ),
        FenetreCom(
            idFenetre = 4,
            datetimeDebut = LocalDateTime.of(2024, 1, 20, 14, 22),
            dureeSecondes = 380,
            elevationMaxDegres = 71.4,
            volumeDonneesMb = null,
            statut = StatutFenetre.PLANIFIEE,
            idSatellite = "SAT-001",
            codeStation = "GS-TLS-01"
        ),
        FenetreCom(
            idFenetre = 5,
            datetimeDebut = LocalDateTime.of(2024, 1, 21, 7, 45),
            dureeSecondes = 290,
            elevationMaxDegres = 59.8,
            volumeDonneesMb = null,
            statut = StatutFenetre.PLANIFIEE,
            idSatellite = "SAT-003",
            codeStation = "GS-TLS-01"
        )
    )

    val embarquements = listOf(
        EmbarquementInstrument(
            idSatellite = "SAT-001",
            refInstrument = "INS-CAM-01",
            dateIntegration = LocalDate.of(2022, 3, 15),
            etatFonctionnement = EtatFonctionnementInstrument.NOMINAL
        ),
        EmbarquementInstrument(
            idSatellite = "SAT-001",
            refInstrument = "INS-IR-01",
            dateIntegration = LocalDate.of(2022, 3, 15),
            etatFonctionnement = EtatFonctionnementInstrument.NOMINAL
        ),
        EmbarquementInstrument(
            idSatellite = "SAT-002",
            refInstrument = "INS-CAM-01",
            dateIntegration = LocalDate.of(2022, 3, 15),
            etatFonctionnement = EtatFonctionnementInstrument.NOMINAL
        ),
        EmbarquementInstrument(
            idSatellite = "SAT-003",
            refInstrument = "INS-CAM-01",
            dateIntegration = LocalDate.of(2023, 6, 10),
            etatFonctionnement = EtatFonctionnementInstrument.NOMINAL
        ),
        EmbarquementInstrument(
            idSatellite = "SAT-003",
            refInstrument = "INS-SPEC-01",
            dateIntegration = LocalDate.of(2023, 6, 10),
            etatFonctionnement = EtatFonctionnementInstrument.NOMINAL
        ),
        EmbarquementInstrument(
            idSatellite = "SAT-004",
            refInstrument = "INS-IR-01",
            dateIntegration = LocalDate.of(2023, 6, 10),
            etatFonctionnement = EtatFonctionnementInstrument.DEGRADE
        ),
        EmbarquementInstrument(
            idSatellite = "SAT-005",
            refInstrument = "INS-AIS-01",
            dateIntegration = LocalDate.of(2021, 11, 20),
            etatFonctionnement = EtatFonctionnementInstrument.HORS_SERVICE
        )
    )
}
