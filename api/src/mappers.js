export function mapOrbite(row) {
  return {
    idOrbite: row.ID_ORBITE,
    typeOrbite: row.TYPE_ORBITE,
    altitudeKm: row.ALTITUDE,
    inclinaisonDegres: row.INCLINAISON,
    periodeOrbitaleMinutes: row.PERIODE_ORBITALE,
    excentricite: row.EXCENTRICITE,
    zoneCouverture: row.ZONE_COUVERTURE
  };
}

export function mapSatellite(row) {
  return {
    idSatellite: row.ID_SATELLITE,
    nomSatellite: row.NOM_SATELLITE,
    dateLancement: row.DATE_LANCEMENT,
    masseKg: row.MASSE,
    formatCubesat: row.FORMAT_CUBESAT,
    statut: row.STATUT,
    dureeViePrevueMois: row.DUREE_VIE_PREVUE,
    capaciteBatterieWh: row.CAPACITE_BATTERIE,
    idOrbite: row.ID_ORBITE
  };
}

export function mapSatelliteOperationnel(row) {
  return {
    idSatellite: row.ID_SATELLITE,
    nomSatellite: row.NOM_SATELLITE,
    nomOrbite: row.NOM_ORBITE,
    nbInstrumentsEmbarques: row.NB_INSTRUMENTS_EMBARQUES,
    statutBatterie: row.STATUT_BATTERIE,
    capaciteBatterieWh: row.CAPACITE_BATTERIE
  };
}

export function mapInstrument(row) {
  return {
    refInstrument: row.REF_INSTRUMENT,
    typeInstrument: row.TYPE_INSTRUMENT,
    modele: row.MODELE,
    resolution: row.RESOLUTION,
    consommationW: row.CONSOMMATION,
    masseKg: row.MASSE
  };
}

export function mapSatelliteInstrument(row) {
  return {
    instrument: mapInstrument(row),
    etatFonctionnement: row.ETAT_FONCTIONNEMENT
  };
}

export function mapStationSol(row) {
  return {
    codeStation: row.CODE_STATION,
    nomStation: row.NOM_STATION,
    latitude: row.LATITUDE,
    longitude: row.LONGITUDE,
    diametreAntenneM: row.DIAMETRE_ANTENNE,
    bandeFrequence: row.BANDE_FREQUENCE,
    debitMaxMbps: row.DEBIT_MAX,
    statut: row.STATUT
  };
}

export function mapMission(row) {
  return {
    idMission: row.ID_MISSION,
    nomMission: row.NOM_MISSION,
    objectif: row.OBJECTIF,
    zoneGeoCible: row.ZONE_GEO_CIBLE,
    dateDebut: row.DATE_DEBUT,
    dateFin: row.DATE_FIN,
    statutMission: row.STATUT_MISSION
  };
}

export function mapMissionStat(row) {
  return {
    idMission: row.ID_MISSION,
    nomMission: row.NOM_MISSION,
    statutMission: row.STATUT_MISSION,
    nbSatellites: row.NB_SATELLITES,
    typesOrbitesRepresentes: row.TYPES_ORBITES_REPRESENTES,
    volumeTotalTelechargeMb: row.VOLUME_TOTAL_TELECHARGE
  };
}

export function mapSatelliteMissionAssignment(row) {
  return {
    mission: mapMission(row),
    roleSatellite: row.ROLE_SATELLITE
  };
}

export function mapFenetreCom(row) {
  return {
    idFenetre: row.ID_FENETRE,
    datetimeDebut: row.DATETIME_DEBUT,
    debutFormate: row.DEBUT_FORMATE ?? null,
    dureeSecondes: row.DUREE,
    dureeFormatee: row.DUREE_FORMATEE ?? null,
    elevationMaxDegres: row.ELEVATION_MAX,
    volumeDonneesMb: row.VOLUME_DONNEES,
    statut: row.STATUT_FENETRE ?? row.STATUT,
    idSatellite: row.ID_SATELLITE,
    nomSatellite: row.NOM_SATELLITE ?? null,
    codeStation: row.CODE_STATION,
    nomStation: row.NOM_STATION ?? null,
    idCentre: row.ID_CENTRE ?? null,
    nomCentre: row.NOM_CENTRE ?? null
  };
}

export function mapVolumeMensuel(row) {
  return {
    moisReference: row.MOIS_REFERENCE,
    idCentre: row.ID_CENTRE ?? null,
    nomCentre: row.NOM_CENTRE ?? null,
    typeSatellite: row.TYPE_SATELLITE,
    nbFenetresRealisees: row.NB_FENETRES_REALISEES,
    volumeTotalMb: row.VOLUME_TOTAL
  };
}
