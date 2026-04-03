SET SERVEROUTPUT ON;

PROMPT ===== Phase 3 - Paliers 1 a 5 =====

-- Exercice 1
DECLARE
    l_nb_satellites NUMBER;
    l_nb_stations NUMBER;
    l_nb_missions NUMBER;
BEGIN
    SELECT COUNT(*) INTO l_nb_satellites FROM satellite;
    SELECT COUNT(*) INTO l_nb_stations FROM station_sol;
    SELECT COUNT(*) INTO l_nb_missions FROM mission;

    DBMS_OUTPUT.PUT_LINE('Bienvenue dans NanoOrbit');
    DBMS_OUTPUT.PUT_LINE('Satellites : ' || l_nb_satellites);
    DBMS_OUTPUT.PUT_LINE('Stations   : ' || l_nb_stations);
    DBMS_OUTPUT.PUT_LINE('Missions   : ' || l_nb_missions);
END;
/

-- Exercice 2
DECLARE
    l_nom satellite.nom_satellite%TYPE;
    l_statut satellite.statut%TYPE;
    l_format satellite.format_cubesat%TYPE;
BEGIN
    SELECT nom_satellite, statut, format_cubesat
      INTO l_nom, l_statut, l_format
      FROM satellite
     WHERE id_satellite = 'SAT-001';

    DBMS_OUTPUT.PUT_LINE('SAT-001 -> ' || l_nom || ', statut=' || l_statut || ', format=' || l_format);
END;
/

-- Exercice 3
DECLARE
    l_sat satellite%ROWTYPE;
BEGIN
    SELECT *
      INTO l_sat
      FROM satellite
     WHERE id_satellite = 'SAT-003';

    DBMS_OUTPUT.PUT_LINE('SAT-003 statut=' || l_sat.statut || ', batterie=' || l_sat.capacite_batterie || ' Wh');
END;
/

-- Exercice 4
-- Attendu : resolution = N/A pour INS-AIS-01.
DECLARE
    l_modele instrument.modele%TYPE;
    l_resolution VARCHAR2(20);
BEGIN
    SELECT modele, NVL(TO_CHAR(resolution), 'N/A')
      INTO l_modele, l_resolution
      FROM instrument
     WHERE ref_instrument = 'INS-AIS-01';

    DBMS_OUTPUT.PUT_LINE('INS-AIS-01 -> modele=' || l_modele || ', resolution=' || l_resolution);
END;
/

-- Exercice 5
DECLARE
    l_statut satellite.statut%TYPE;
    l_date_lancement satellite.date_lancement%TYPE;
    l_duree_vie satellite.duree_vie_prevue%TYPE;
    l_restant NUMBER;
BEGIN
    SELECT statut, date_lancement, duree_vie_prevue
      INTO l_statut, l_date_lancement, l_duree_vie
      FROM satellite
     WHERE id_satellite = 'SAT-001';

    l_restant := ROUND(l_duree_vie - MONTHS_BETWEEN(SYSDATE, l_date_lancement));

    IF l_statut = 'Désorbité' THEN
        DBMS_OUTPUT.PUT_LINE('SAT-001 -> satellite hors service definitif');
    ELSIF l_restant <= 12 THEN
        DBMS_OUTPUT.PUT_LINE('SAT-001 -> surveillance critique, ' || l_restant || ' mois restants');
    ELSIF l_restant <= 24 THEN
        DBMS_OUTPUT.PUT_LINE('SAT-001 -> surveillance rapprochee, ' || l_restant || ' mois restants');
    ELSE
        DBMS_OUTPUT.PUT_LINE('SAT-001 -> exploitation nominale, ' || l_restant || ' mois restants');
    END IF;
END;
/

-- Exercice 6
DECLARE
    l_type_orbite orbite.type_orbite%TYPE;
    l_altitude orbite.altitude%TYPE;
    l_periode orbite.periode_orbitale%TYPE;
    l_vitesse NUMBER;
BEGIN
    SELECT o.type_orbite, o.altitude, o.periode_orbitale
      INTO l_type_orbite, l_altitude, l_periode
      FROM satellite s
      JOIN orbite o ON o.id_orbite = s.id_orbite
     WHERE s.id_satellite = 'SAT-001';

    l_vitesse := ROUND((2 * ACOS(-1) * (6371 + l_altitude)) / l_periode, 2);

    CASE l_type_orbite
        WHEN 'SSO' THEN DBMS_OUTPUT.PUT_LINE('SAT-001 -> orbite SSO');
        WHEN 'LEO' THEN DBMS_OUTPUT.PUT_LINE('SAT-001 -> orbite LEO');
        WHEN 'MEO' THEN DBMS_OUTPUT.PUT_LINE('SAT-001 -> orbite MEO');
        WHEN 'GEO' THEN DBMS_OUTPUT.PUT_LINE('SAT-001 -> orbite GEO');
        ELSE DBMS_OUTPUT.PUT_LINE('SAT-001 -> type d''orbite inconnu');
    END CASE;

    DBMS_OUTPUT.PUT_LINE('Vitesse orbitale approx. = ' || l_vitesse || ' km/min');
END;
/

-- Exercice 7
DECLARE
    l_debit station_sol.debit_max%TYPE;
    l_volume_mb NUMBER;
BEGIN
    SELECT debit_max
      INTO l_debit
      FROM station_sol
     WHERE code_station = 'GS-TLS-01';

    FOR l_minutes IN 5 .. 15 LOOP
        l_volume_mb := ROUND((l_debit * (l_minutes * 60)) / 8, 2);
        DBMS_OUTPUT.PUT_LINE(l_minutes || ' min -> ' || l_volume_mb || ' MB theoriques');
    END LOOP;
END;
/

-- Exercice 8
DECLARE
BEGIN
    SAVEPOINT ex8_before_update;

    UPDATE satellite
       SET statut = 'En veille'
     WHERE id_satellite IN ('SAT-001', 'SAT-002');

    DBMS_OUTPUT.PUT_LINE('Ex8 -> lignes modifiees = ' || SQL%ROWCOUNT);

    ROLLBACK TO ex8_before_update;
END;
/

-- Exercice 9
DECLARE
    CURSOR c_satellites IS
        SELECT s.id_satellite,
               s.nom_satellite,
               o.type_orbite,
               s.statut,
               NVL(LISTAGG(i.ref_instrument, ', ') WITHIN GROUP (ORDER BY i.ref_instrument), 'Aucun') AS instruments
          FROM satellite s
          JOIN orbite o ON o.id_orbite = s.id_orbite
          LEFT JOIN embarquement e ON e.id_satellite = s.id_satellite
          LEFT JOIN instrument i ON i.ref_instrument = e.ref_instrument
         GROUP BY s.id_satellite, s.nom_satellite, o.type_orbite, s.statut
         ORDER BY s.id_satellite;
BEGIN
    FOR r_sat IN c_satellites LOOP
        DBMS_OUTPUT.PUT_LINE(
            r_sat.id_satellite || ' | ' || r_sat.nom_satellite || ' | ' ||
            r_sat.type_orbite || ' | ' || r_sat.statut || ' | ' || r_sat.instruments
        );
    END LOOP;
END;
/

-- Exercice 10
DECLARE
    CURSOR c_satellites_op IS
        SELECT s.id_satellite,
               s.nom_satellite,
               NVL((
                   SELECT fc.code_station
                     FROM fenetre_com fc
                    WHERE fc.id_satellite = s.id_satellite
                    ORDER BY fc.datetime_debut DESC
                    FETCH FIRST 1 ROW ONLY
               ), 'Aucune') AS derniere_station
          FROM satellite s
         WHERE s.statut = 'Opérationnel'
         ORDER BY s.id_satellite;

    l_id satellite.id_satellite%TYPE;
    l_nom satellite.nom_satellite%TYPE;
    l_station station_sol.code_station%TYPE;
BEGIN
    OPEN c_satellites_op;
    LOOP
        FETCH c_satellites_op INTO l_id, l_nom, l_station;
        EXIT WHEN c_satellites_op%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE(l_id || ' | ' || l_nom || ' | derniere station = ' || l_station);
    END LOOP;
    CLOSE c_satellites_op;
END;
/

-- Exercice 11
DECLARE
    CURSOR c_fenetres_station(p_code_station station_sol.code_station%TYPE) IS
        SELECT id_fenetre, datetime_debut, statut, NVL(volume_donnees, 0) AS volume_donnees
          FROM fenetre_com
         WHERE code_station = p_code_station
         ORDER BY datetime_debut;

    l_total NUMBER := 0;
BEGIN
    FOR r_fen IN c_fenetres_station('GS-TLS-01') LOOP
        l_total := l_total + r_fen.volume_donnees;
        DBMS_OUTPUT.PUT_LINE(
            'Fenetre ' || r_fen.id_fenetre || ' | ' ||
            TO_CHAR(r_fen.datetime_debut, 'YYYY-MM-DD HH24:MI') || ' | ' ||
            r_fen.statut || ' | volume=' || r_fen.volume_donnees
        );
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('Volume total GS-TLS-01 = ' || l_total);
END;
/

CREATE OR REPLACE PROCEDURE afficher_satellite_securise(
    p_id IN satellite.id_satellite%TYPE
) IS
    l_sat satellite%ROWTYPE;
BEGIN
    SELECT *
      INTO l_sat
      FROM satellite
     WHERE id_satellite = p_id;

    DBMS_OUTPUT.PUT_LINE('Satellite ' || l_sat.id_satellite || ' -> ' || l_sat.nom_satellite || ' | ' || l_sat.statut);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Aucun satellite trouve pour id=' || p_id);
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erreur inattendue : ' || SQLERRM);
END;
/
SHOW ERRORS PROCEDURE afficher_satellite_securise;

-- Exercice 12
BEGIN
    afficher_satellite_securise('SAT-001');
    afficher_satellite_securise('SAT-999');
END;
/

CREATE OR REPLACE PROCEDURE verifier_fenetre_communication(
    p_id_satellite IN satellite.id_satellite%TYPE,
    p_code_station IN station_sol.code_station%TYPE,
    p_datetime_debut IN fenetre_com.datetime_debut%TYPE,
    p_duree IN fenetre_com.duree%TYPE
) IS
    l_statut_satellite satellite.statut%TYPE;
    l_statut_station station_sol.statut%TYPE;
    l_overlap_sat NUMBER;
    l_overlap_sta NUMBER;
BEGIN
    IF p_duree NOT BETWEEN 1 AND 900 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Durée invalide : elle doit etre comprise entre 1 et 900 secondes.');
    END IF;

    SELECT statut
      INTO l_statut_satellite
      FROM satellite
     WHERE id_satellite = p_id_satellite;

    IF l_statut_satellite <> 'Opérationnel' THEN
        RAISE_APPLICATION_ERROR(-20012, 'Le satellite doit etre Opérationnel pour planifier une fenetre.');
    END IF;

    SELECT statut
      INTO l_statut_station
      FROM station_sol
     WHERE code_station = p_code_station;

    IF l_statut_station <> 'Active' THEN
        RAISE_APPLICATION_ERROR(-20013, 'La station doit etre Active pour planifier une fenetre.');
    END IF;

    SELECT COUNT(*)
      INTO l_overlap_sat
      FROM fenetre_com fc
     WHERE fc.id_satellite = p_id_satellite
       AND fc.datetime_debut < p_datetime_debut + NUMTODSINTERVAL(p_duree, 'SECOND')
       AND fc.datetime_debut + NUMTODSINTERVAL(fc.duree, 'SECOND') > p_datetime_debut;

    IF l_overlap_sat > 0 THEN
        RAISE_APPLICATION_ERROR(-20014, 'Chevauchement detecte pour le satellite.');
    END IF;

    SELECT COUNT(*)
      INTO l_overlap_sta
      FROM fenetre_com fc
     WHERE fc.code_station = p_code_station
       AND fc.datetime_debut < p_datetime_debut + NUMTODSINTERVAL(p_duree, 'SECOND')
       AND fc.datetime_debut + NUMTODSINTERVAL(fc.duree, 'SECOND') > p_datetime_debut;

    IF l_overlap_sta > 0 THEN
        RAISE_APPLICATION_ERROR(-20015, 'Chevauchement detecte pour la station.');
    END IF;

    DBMS_OUTPUT.PUT_LINE('Fenetre validee pour insertion.');
END;
/
SHOW ERRORS PROCEDURE verifier_fenetre_communication;

-- Exercice 13
BEGIN
    verifier_fenetre_communication(
        'SAT-001',
        'GS-KIR-01',
        TO_TIMESTAMP('2024-01-22 14:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        240
    );
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Cas valide inattendu : ' || SQLERRM);
END;
/

BEGIN
    verifier_fenetre_communication(
        'SAT-004',
        'GS-TLS-01',
        TO_TIMESTAMP('2024-01-15 09:20:00', 'YYYY-MM-DD HH24:MI:SS'),
        240
    );
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Cas erreur attendu : ' || SQLERRM);
END;
/

CREATE OR REPLACE PROCEDURE afficher_statut_satellite(
    p_id IN satellite.id_satellite%TYPE
) IS
    l_nom satellite.nom_satellite%TYPE;
    l_statut satellite.statut%TYPE;
    l_type_orbite orbite.type_orbite%TYPE;
    l_instruments VARCHAR2(4000);
BEGIN
    SELECT s.nom_satellite, s.statut, o.type_orbite
      INTO l_nom, l_statut, l_type_orbite
      FROM satellite s
      JOIN orbite o ON o.id_orbite = s.id_orbite
     WHERE s.id_satellite = p_id;

    SELECT NVL(LISTAGG(i.ref_instrument, ', ') WITHIN GROUP (ORDER BY i.ref_instrument), 'Aucun')
      INTO l_instruments
      FROM embarquement e
      JOIN instrument i ON i.ref_instrument = e.ref_instrument
     WHERE e.id_satellite = p_id;

    DBMS_OUTPUT.PUT_LINE('Satellite : ' || p_id || ' - ' || l_nom);
    DBMS_OUTPUT.PUT_LINE('Statut    : ' || l_statut);
    DBMS_OUTPUT.PUT_LINE('Orbite    : ' || l_type_orbite);
    DBMS_OUTPUT.PUT_LINE('Instruments : ' || l_instruments);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Satellite introuvable : ' || p_id);
END;
/
SHOW ERRORS PROCEDURE afficher_statut_satellite;

-- Exercice 14
BEGIN
    afficher_statut_satellite('SAT-001');
END;
/

CREATE OR REPLACE PROCEDURE mettre_a_jour_statut(
    p_id IN satellite.id_satellite%TYPE,
    p_statut IN satellite.statut%TYPE,
    p_ancien_statut OUT satellite.statut%TYPE
) IS
BEGIN
    SELECT statut
      INTO p_ancien_statut
      FROM satellite
     WHERE id_satellite = p_id;

    UPDATE satellite
       SET statut = p_statut
     WHERE id_satellite = p_id;

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(-20016, 'Aucune mise a jour effectuee pour ' || p_id);
    END IF;
END;
/
SHOW ERRORS PROCEDURE mettre_a_jour_statut;

-- Exercice 15
DECLARE
    l_ancien satellite.statut%TYPE;
BEGIN
    SAVEPOINT ex15_before_update;

    mettre_a_jour_statut('SAT-004', 'Défaillant', l_ancien);
    DBMS_OUTPUT.PUT_LINE('Ancien statut SAT-004 = ' || l_ancien);

    ROLLBACK TO ex15_before_update;
END;
/

CREATE OR REPLACE FUNCTION calculer_volume_session(
    p_id_fenetre IN fenetre_com.id_fenetre%TYPE
) RETURN NUMBER IS
    l_debit station_sol.debit_max%TYPE;
    l_duree fenetre_com.duree%TYPE;
BEGIN
    SELECT st.debit_max, fc.duree
      INTO l_debit, l_duree
      FROM fenetre_com fc
      JOIN station_sol st ON st.code_station = fc.code_station
     WHERE fc.id_fenetre = p_id_fenetre;

    RETURN ROUND((l_debit * l_duree) / 8, 2);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
END;
/
SHOW ERRORS FUNCTION calculer_volume_session;

-- Exercice 16
DECLARE
    l_volume NUMBER;
BEGIN
    l_volume := calculer_volume_session(1);
    DBMS_OUTPUT.PUT_LINE('Volume theorique fenetre 1 = ' || NVL(TO_CHAR(l_volume), 'NULL'));
END;
/

PROMPT ===== Fin Phase 3 - Paliers 1 a 5 =====
