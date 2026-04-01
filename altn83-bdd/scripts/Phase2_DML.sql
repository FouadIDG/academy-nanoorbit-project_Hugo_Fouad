
INSERT INTO orbite (
    id_orbite,
    type_orbite,
    altitude,
    inclinaison,
    periode_orbitale,
    excentricite,
    zone_couverture
) VALUES (
    1,
    'SSO',
    550,
    97.6,
    95.5,
    0.0010,
    'Polaire globale — Europe / Arctique'
);

INSERT INTO orbite (
    id_orbite,
    type_orbite,
    altitude,
    inclinaison,
    periode_orbitale,
    excentricite,
    zone_couverture
) VALUES (
    2,
    'SSO',
    700,
    98.2,
    98.8,
    0.0008,
    'Polaire globale — haute latitude'
);

INSERT INTO orbite (
    id_orbite,
    type_orbite,
    altitude,
    inclinaison,
    periode_orbitale,
    excentricite,
    zone_couverture
) VALUES (
    3,
    'LEO',
    400,
    51.6,
    92.6,
    0.0020,
    'Équatoriale — zone tropicale'
);

INSERT INTO satellite (
    id_satellite,
    nom_satellite,
    date_lancement,
    masse,
    format_cubesat,
    statut,
    duree_vie_prevue,
    capacite_batterie,
    id_orbite
) VALUES (
    'SAT-001',
    'NanoOrbit-Alpha',
    TO_DATE('2022-03-15', 'YYYY-MM-DD'),
    1.30,
    '3U',
    'Opérationnel',
    60,
    20,
    1
);

INSERT INTO satellite VALUES (
    'SAT-002',
    'NanoOrbit-Beta',
    TO_DATE('2022-03-15', 'YYYY-MM-DD'),
    1.30,
    '3U',
    'Opérationnel',
    60,
    20,
    1
);

INSERT INTO satellite VALUES (
    'SAT-003',
    'NanoOrbit-Gamma',
    TO_DATE('2023-06-10', 'YYYY-MM-DD'),
    2.00,
    '6U',
    'Opérationnel',
    84,
    40,
    2
);

INSERT INTO satellite VALUES (
    'SAT-004',
    'NanoOrbit-Delta',
    TO_DATE('2023-06-10', 'YYYY-MM-DD'),
    2.00,
    '6U',
    'En veille',
    84,
    40,
    2
);

INSERT INTO satellite VALUES (
    'SAT-005',
    'NanoOrbit-Epsilon',
    TO_DATE('2021-11-20', 'YYYY-MM-DD'),
    4.50,
    '12U',
    'Désorbité',
    36,
    80,
    3
);

INSERT INTO instrument VALUES (
    'INS-CAM-01',
    'Caméra optique',
    'PlanetScope-Mini',
    3,
    2.5,
    0.400
);

INSERT INTO instrument VALUES (
    'INS-IR-01',
    'Infrarouge',
    'FLIR-Lepton-3',
    160,
    1.2,
    0.150
);

INSERT INTO instrument VALUES (
    'INS-AIS-01',
    'Récepteur AIS',
    'ShipTrack-V2',
    NULL,
    0.8,
    0.120
);

INSERT INTO instrument VALUES (
    'INS-SPEC-01',
    'Spectromètre',
    'HyperSpec-Nano',
    30,
    3.1,
    0.600
);

INSERT INTO embarquement VALUES (
    'SAT-001',
    'INS-CAM-01',
    TO_DATE('2022-03-15', 'YYYY-MM-DD'),
    'Nominal'
);

INSERT INTO embarquement VALUES (
    'SAT-001',
    'INS-IR-01',
    TO_DATE('2022-03-15', 'YYYY-MM-DD'),
    'Nominal'
);

INSERT INTO embarquement VALUES (
    'SAT-002',
    'INS-CAM-01',
    TO_DATE('2022-03-15', 'YYYY-MM-DD'),
    'Nominal'
);

INSERT INTO embarquement VALUES (
    'SAT-003',
    'INS-CAM-01',
    TO_DATE('2023-06-10', 'YYYY-MM-DD'),
    'Nominal'
);

INSERT INTO embarquement VALUES (
    'SAT-003',
    'INS-SPEC-01',
    TO_DATE('2023-06-10', 'YYYY-MM-DD'),
    'Nominal'
);

INSERT INTO embarquement VALUES (
    'SAT-004',
    'INS-IR-01',
    TO_DATE('2023-06-10', 'YYYY-MM-DD'),
    'Dégradé'
);

INSERT INTO embarquement VALUES (
    'SAT-005',
    'INS-AIS-01',
    TO_DATE('2021-11-20', 'YYYY-MM-DD'),
    'Hors service'
);

INSERT INTO centre_controle VALUES (
    1,
    'NanoOrbit Paris HQ',
    'Paris',
    'Europe',
    'Europe/Paris',
    'Actif'
);

INSERT INTO centre_controle VALUES (
    2,
    'NanoOrbit Houston',
    'Houston',
    'Amériques',
    'America/Chicago',
    'Actif'
);

INSERT INTO station_sol VALUES (
    'GS-TLS-01',
    'Toulouse Ground Station',
    43.604700,
    1.444200,
    3.5,
    'S',
    150,
    'Active'
);

INSERT INTO station_sol VALUES (
    'GS-KIR-01',
    'Kiruna Arctic Station',
    67.855700,
    20.225300,
    5.4,
    'X',
    400,
    'Active'
);

INSERT INTO station_sol VALUES (
    'GS-SGP-01',
    'Singapore Station',
    1.352100,
    103.819800,
    3.0,
    'S',
    120,
    'Maintenance'
);

INSERT INTO affectation_station VALUES (
    1,
    'GS-TLS-01',
    TO_DATE('2022-01-10', 'YYYY-MM-DD')
);

INSERT INTO affectation_station VALUES (
    1,
    'GS-KIR-01',
    TO_DATE('2022-01-10', 'YYYY-MM-DD')
);

INSERT INTO affectation_station VALUES (
    2,
    'GS-SGP-01',
    TO_DATE('2023-03-15', 'YYYY-MM-DD')
);

INSERT INTO mission VALUES (
    'MSN-ARC-2023',
    'ArcticWatch 2023',
    'Surveillance fonte des glaces et dynamique des banquises',
    'Arctique / Groenland',
    TO_DATE('2023-01-01', 'YYYY-MM-DD'),
    NULL,
    'Active'
);

INSERT INTO mission VALUES (
    'MSN-DEF-2022',
    'DeforestAlert',
    'Détection et cartographie de la déforestation en temps quasi-réel',
    'Amazonie / Congo',
    TO_DATE('2022-06-01', 'YYYY-MM-DD'),
    TO_DATE('2023-05-31', 'YYYY-MM-DD'),
    'Terminée'
);

INSERT INTO mission VALUES (
    'MSN-COAST-2024',
    'CoastGuard 2024',
    'Surveillance évolution du trait de côte et détection d''érosion',
    'Méditerranée / Atlantique',
    TO_DATE('2024-03-01', 'YYYY-MM-DD'),
    NULL,
    'Active'
);

INSERT INTO fenetre_com VALUES (
    1,
    TO_TIMESTAMP('2024-01-15 09:14:00', 'YYYY-MM-DD HH24:MI:SS'),
    420,
    82.3,
    1250,
    'Réalisée',
    'SAT-001',
    'GS-KIR-01'
);

INSERT INTO fenetre_com VALUES (
    2,
    TO_TIMESTAMP('2024-01-15 11:52:00', 'YYYY-MM-DD HH24:MI:SS'),
    310,
    67.1,
    890,
    'Réalisée',
    'SAT-002',
    'GS-TLS-01'
);

INSERT INTO fenetre_com VALUES (
    3,
    TO_TIMESTAMP('2024-01-16 08:30:00', 'YYYY-MM-DD HH24:MI:SS'),
    540,
    88.9,
    1680,
    'Réalisée',
    'SAT-003',
    'GS-KIR-01'
);

INSERT INTO fenetre_com VALUES (
    4,
    TO_TIMESTAMP('2024-01-20 14:22:00', 'YYYY-MM-DD HH24:MI:SS'),
    380,
    71.4,
    NULL,
    'Planifiée',
    'SAT-001',
    'GS-TLS-01'
);

INSERT INTO fenetre_com VALUES (
    5,
    TO_TIMESTAMP('2024-01-21 07:45:00', 'YYYY-MM-DD HH24:MI:SS'),
    290,
    59.8,
    NULL,
    'Planifiée',
    'SAT-003',
    'GS-TLS-01'
);

INSERT INTO participation VALUES (
    'SAT-001',
    'MSN-ARC-2023',
    'Imageur principal'
);

INSERT INTO participation VALUES (
    'SAT-002',
    'MSN-ARC-2023',
    'Imageur secondaire'
);

INSERT INTO participation VALUES (
    'SAT-003',
    'MSN-ARC-2023',
    'Satellite de relais'
);

INSERT INTO participation VALUES (
    'SAT-001',
    'MSN-DEF-2022',
    'Imageur principal'
);

INSERT INTO participation VALUES (
    'SAT-005',
    'MSN-DEF-2022',
    'Imageur secondaire'
);

INSERT INTO participation VALUES (
    'SAT-003',
    'MSN-COAST-2024',
    'Imageur principal'
);

INSERT INTO participation VALUES (
    'SAT-004',
    'MSN-COAST-2024',
    'Satellite de secours'
);

COMMIT;
