import { executeQuery } from '../database.js';
import {
  mapFenetreCom,
  mapMissionStat,
  mapOrbite,
  mapSatellite,
  mapSatelliteInstrument,
  mapSatelliteMissionAssignment,
  mapSatelliteOperationnel,
  mapStationSol,
  mapVolumeMensuel
} from '../mappers.js';

const DATE_FORMAT = "YYYY-MM-DD";
const TIMESTAMP_FORMAT = "YYYY-MM-DD\"T\"HH24:MI:SS";

export async function getOrbites() {
  const result = await executeQuery(`
    SELECT
      id_orbite,
      type_orbite,
      altitude,
      inclinaison,
      periode_orbitale,
      excentricite,
      zone_couverture
    FROM orbite
    ORDER BY id_orbite
  `);

  return result.rows.map(mapOrbite);
}

export async function getStations() {
  const result = await executeQuery(`
    SELECT
      code_station,
      nom_station,
      latitude,
      longitude,
      diametre_antenne,
      bande_frequence,
      debit_max,
      statut
    FROM station_sol
    ORDER BY code_station
  `);

  return result.rows.map(mapStationSol);
}

export async function getSatellites() {
  const result = await executeQuery(`
    SELECT
      id_satellite,
      nom_satellite,
      TO_CHAR(date_lancement, '${DATE_FORMAT}') AS date_lancement,
      masse,
      format_cubesat,
      statut,
      duree_vie_prevue,
      capacite_batterie,
      id_orbite
    FROM satellite
    ORDER BY id_satellite
  `);

  return result.rows.map(mapSatellite);
}

export async function getSatelliteById(satelliteId) {
  const result = await executeQuery(
    `
      SELECT
        id_satellite,
        nom_satellite,
        TO_CHAR(date_lancement, '${DATE_FORMAT}') AS date_lancement,
        masse,
        format_cubesat,
        statut,
        duree_vie_prevue,
        capacite_batterie,
        id_orbite
      FROM satellite
      WHERE id_satellite = :satelliteId
    `,
    { satelliteId }
  );

  return result.rows[0] ? mapSatellite(result.rows[0]) : null;
}

export async function getSatellitesOperationnels() {
  const result = await executeQuery(`
    SELECT
      id_satellite,
      nom_satellite,
      nom_orbite,
      nb_instruments_embarques,
      statut_batterie,
      capacite_batterie
    FROM v_satellites_operationnels
    ORDER BY id_satellite
  `);

  return result.rows.map(mapSatelliteOperationnel);
}

export async function getSatelliteInstruments(satelliteId) {
  const result = await executeQuery(
    `
      SELECT
        i.ref_instrument,
        i.type_instrument,
        i.modele,
        i.resolution,
        i.consommation,
        i.masse,
        e.etat_fonctionnement
      FROM embarquement e
      JOIN instrument i
        ON i.ref_instrument = e.ref_instrument
      WHERE e.id_satellite = :satelliteId
      ORDER BY i.ref_instrument
    `,
    { satelliteId }
  );

  return result.rows.map(mapSatelliteInstrument);
}

export async function getSatelliteMissions(satelliteId) {
  const result = await executeQuery(
    `
      SELECT
        m.id_mission,
        m.nom_mission,
        m.objectif,
        m.zone_geo_cible,
        TO_CHAR(m.date_debut, '${DATE_FORMAT}') AS date_debut,
        TO_CHAR(m.date_fin, '${DATE_FORMAT}') AS date_fin,
        m.statut_mission,
        p.role_satellite
      FROM participation p
      JOIN mission m
        ON m.id_mission = p.id_mission
      WHERE p.id_satellite = :satelliteId
      ORDER BY m.id_mission
    `,
    { satelliteId }
  );

  return result.rows.map(mapSatelliteMissionAssignment);
}

export async function getMissionStats() {
  const result = await executeQuery(`
    SELECT
      id_mission,
      nom_mission,
      statut_mission,
      nb_satellites,
      types_orbites_representes,
      volume_total_telecharge
    FROM v_stats_missions
    ORDER BY id_mission
  `);

  return result.rows.map(mapMissionStat);
}

export async function getFenetres() {
  const result = await executeQuery(`
    SELECT
      v.id_fenetre,
      TO_CHAR(v.datetime_debut, '${TIMESTAMP_FORMAT}') AS datetime_debut,
      v.debut_formate,
      v.duree,
      v.duree_formatee,
      v.elevation_max,
      v.volume_donnees,
      v.statut_fenetre,
      v.id_satellite,
      v.nom_satellite,
      v.code_station,
      v.nom_station,
      v.id_centre,
      v.nom_centre
    FROM v_fenetres_detail v
    ORDER BY v.datetime_debut, v.id_fenetre
  `);

  return result.rows.map(mapFenetreCom);
}

export async function getVolumesMensuels() {
  const result = await executeQuery(`
    SELECT
      TO_CHAR(mois_reference, '${DATE_FORMAT}') AS mois_reference,
      id_centre,
      nom_centre,
      type_satellite,
      nb_fenetres_realisees,
      volume_total
    FROM mv_volumes_mensuels
    ORDER BY mois_reference DESC, id_centre, type_satellite
  `);

  return result.rows.map(mapVolumeMensuel);
}

export async function validateFenetreRequest({
  satelliteId,
  codeStation,
  dureeSecondes
}) {
  if (!satelliteId || !codeStation || !Number.isInteger(dureeSecondes)) {
    return {
      isValid: false,
      message: 'satelliteId, codeStation et dureeSecondes sont obligatoires.'
    };
  }

  if (dureeSecondes < 1 || dureeSecondes > 900) {
    return {
      isValid: false,
      message: 'Durée invalide : elle doit être comprise entre 1 et 900 secondes.'
    };
  }

  const satellite = await getSatelliteById(satelliteId);
  if (!satellite) {
    return {
      isValid: false,
      message: `Satellite introuvable : ${satelliteId}`
    };
  }

  if (satellite.statut === 'Désorbité') {
    return {
      isValid: false,
      message: 'Insertion refusée : le satellite est désorbité.'
    };
  }

  const stationResult = await executeQuery(
    `
      SELECT statut
      FROM station_sol
      WHERE code_station = :codeStation
    `,
    { codeStation }
  );

  const station = stationResult.rows[0];
  if (!station) {
    return {
      isValid: false,
      message: `Station introuvable : ${codeStation}`
    };
  }

  if (station.STATUT === 'Maintenance') {
    return {
      isValid: false,
      message: 'Insertion refusée : la station est en maintenance.'
    };
  }

  return {
    isValid: true,
    message: `Fenêtre planifiable pour ${satelliteId} depuis ${codeStation}.`
  };
}
