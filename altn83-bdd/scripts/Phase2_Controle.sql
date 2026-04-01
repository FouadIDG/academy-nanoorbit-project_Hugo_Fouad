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
