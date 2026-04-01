SELECT USER AS current_user FROM dual;

SELECT table_name
FROM user_tables
WHERE table_name IN (
    'ORBITE',
    'SATELLITE',
    'HISTORIQUE_STATUT',
    'INSTRUMENT',
    'EMBARQUEMENT',
    'CENTRE_CONTROLE',
    'STATION_SOL',
    'AFFECTATION_STATION',
    'MISSION',
    'FENETRE_COM',
    'PARTICIPATION'
)
ORDER BY table_name;

SELECT constraint_name, constraint_type, table_name
FROM user_constraints
WHERE table_name IN (
    'ORBITE',
    'SATELLITE',
    'HISTORIQUE_STATUT',
    'INSTRUMENT',
    'EMBARQUEMENT',
    'CENTRE_CONTROLE',
    'STATION_SOL',
    'AFFECTATION_STATION',
    'MISSION',
    'FENETRE_COM',
    'PARTICIPATION'
)
AND constraint_name NOT LIKE 'SYS_%'
ORDER BY table_name, constraint_type, constraint_name;

SELECT trigger_name, status, triggering_event, trigger_type, table_name
FROM user_triggers
WHERE trigger_name IN (
    'TRG_VALIDER_FENETRE',
    'TRG_NO_CHEVAUCHEMENT',
    'TRG_VOLUME_REALISE',
    'TRG_MISSION_TERMINEE',
    'TRG_HISTORIQUE_STATUT'
)
ORDER BY trigger_name;

SELECT name, type, line, position, text
FROM user_errors
WHERE type = 'TRIGGER'
ORDER BY name, sequence;

SELECT 'AFFECTATION_STATION' AS table_name, COUNT(*) AS nb FROM affectation_station
UNION ALL
SELECT 'CENTRE_CONTROLE' AS table_name, COUNT(*) AS nb FROM centre_controle
UNION ALL
SELECT 'EMBARQUEMENT', COUNT(*) FROM embarquement
UNION ALL
SELECT 'FENETRE_COM', COUNT(*) FROM fenetre_com
UNION ALL
SELECT 'HISTORIQUE_STATUT', COUNT(*) FROM historique_statut
UNION ALL
SELECT 'INSTRUMENT', COUNT(*) FROM instrument
UNION ALL
SELECT 'MISSION', COUNT(*) FROM mission
UNION ALL
SELECT 'ORBITE', COUNT(*) FROM orbite
UNION ALL
SELECT 'PARTICIPATION', COUNT(*) FROM participation
UNION ALL
SELECT 'SATELLITE', COUNT(*) FROM satellite
UNION ALL
SELECT 'STATION_SOL', COUNT(*) FROM station_sol
ORDER BY 1;

SELECT statut, COUNT(*) AS nb
FROM satellite
GROUP BY statut
ORDER BY statut;

SELECT statut, COUNT(*) AS nb
FROM station_sol
GROUP BY statut
ORDER BY statut;

SELECT statut_mission, COUNT(*) AS nb
FROM mission
GROUP BY statut_mission
ORDER BY statut_mission;
