SET SERVEROUTPUT ON;

PROMPT ===== Creation des triggers NanoOrbit =====

CREATE OR REPLACE TRIGGER trg_valider_fenetre
    BEFORE INSERT ON fenetre_com
    FOR EACH ROW
DECLARE
    l_statut_satellite satellite.statut%TYPE;
    l_statut_station station_sol.statut%TYPE;
BEGIN
    SELECT statut
      INTO l_statut_satellite
      FROM satellite
     WHERE id_satellite = :NEW.id_satellite;

    IF l_statut_satellite = 'Désorbité' THEN
        RAISE_APPLICATION_ERROR(-20001, 'Insertion refusée : le satellite est désorbité.');
    END IF;

    SELECT statut
      INTO l_statut_station
      FROM station_sol
     WHERE code_station = :NEW.code_station;

    IF l_statut_station = 'Maintenance' THEN
        RAISE_APPLICATION_ERROR(-20002, 'Insertion refusée : la station est en maintenance.');
    END IF;
END;
/
SHOW ERRORS TRIGGER trg_valider_fenetre;


CREATE OR REPLACE TRIGGER trg_no_chevauchement
    FOR INSERT OR UPDATE ON fenetre_com
COMPOUND TRIGGER

    TYPE t_fenetre_rec IS RECORD (
        id_fenetre      fenetre_com.id_fenetre%TYPE,
        id_satellite    fenetre_com.id_satellite%TYPE,
        code_station    fenetre_com.code_station%TYPE,
        datetime_debut  fenetre_com.datetime_debut%TYPE,
        duree           fenetre_com.duree%TYPE
    );

    TYPE t_fenetre_tab IS TABLE OF t_fenetre_rec INDEX BY PLS_INTEGER;

    g_fenetres t_fenetre_tab;
    g_count    PLS_INTEGER := 0;

    AFTER EACH ROW IS
    BEGIN
        g_count := g_count + 1;
        g_fenetres(g_count).id_fenetre := :NEW.id_fenetre;
        g_fenetres(g_count).id_satellite := :NEW.id_satellite;
        g_fenetres(g_count).code_station := :NEW.code_station;
        g_fenetres(g_count).datetime_debut := :NEW.datetime_debut;
        g_fenetres(g_count).duree := :NEW.duree;
    END AFTER EACH ROW;

    AFTER STATEMENT IS
        l_nb_satellite NUMBER;
        l_nb_station   NUMBER;
    BEGIN
        FOR i IN 1 .. g_count LOOP
            SELECT COUNT(*)
              INTO l_nb_satellite
              FROM fenetre_com fc
             WHERE fc.id_satellite = g_fenetres(i).id_satellite
               AND fc.datetime_debut < g_fenetres(i).datetime_debut + NUMTODSINTERVAL(g_fenetres(i).duree, 'SECOND')
               AND fc.datetime_debut + NUMTODSINTERVAL(fc.duree, 'SECOND') > g_fenetres(i).datetime_debut
               AND (
                    g_fenetres(i).id_fenetre IS NULL
                    OR fc.id_fenetre <> g_fenetres(i).id_fenetre
               );

            IF (g_fenetres(i).id_fenetre IS NULL AND l_nb_satellite > 1)
               OR (g_fenetres(i).id_fenetre IS NOT NULL AND l_nb_satellite > 0) THEN
                RAISE_APPLICATION_ERROR(-20003, 'Chevauchement temporel detecte pour le satellite.');
            END IF;

            SELECT COUNT(*)
              INTO l_nb_station
              FROM fenetre_com fc
             WHERE fc.code_station = g_fenetres(i).code_station
               AND fc.datetime_debut < g_fenetres(i).datetime_debut + NUMTODSINTERVAL(g_fenetres(i).duree, 'SECOND')
               AND fc.datetime_debut + NUMTODSINTERVAL(fc.duree, 'SECOND') > g_fenetres(i).datetime_debut
               AND (
                    g_fenetres(i).id_fenetre IS NULL
                    OR fc.id_fenetre <> g_fenetres(i).id_fenetre
               );

            IF (g_fenetres(i).id_fenetre IS NULL AND l_nb_station > 1)
               OR (g_fenetres(i).id_fenetre IS NOT NULL AND l_nb_station > 0) THEN
                RAISE_APPLICATION_ERROR(-20003, 'Chevauchement temporel detecte pour la station.');
            END IF;
        END LOOP;
    END AFTER STATEMENT;

END trg_no_chevauchement;
/
SHOW ERRORS TRIGGER trg_no_chevauchement;


CREATE OR REPLACE TRIGGER trg_volume_realise
    BEFORE INSERT OR UPDATE ON fenetre_com
    FOR EACH ROW
BEGIN
    IF :NEW.statut <> 'Réalisée' THEN
        :NEW.volume_donnees := NULL;
    END IF;
END;
/
SHOW ERRORS TRIGGER trg_volume_realise;


CREATE OR REPLACE TRIGGER trg_mission_terminee
    BEFORE INSERT ON participation
    FOR EACH ROW
DECLARE
    l_statut_mission   mission.statut_mission%TYPE;
    l_statut_satellite satellite.statut%TYPE;
BEGIN
    SELECT statut_mission
      INTO l_statut_mission
      FROM mission
     WHERE id_mission = :NEW.id_mission;

    IF l_statut_mission = 'Terminée' THEN
        RAISE_APPLICATION_ERROR(-20004, 'Insertion refusée : la mission est terminee.');
    END IF;

    SELECT statut
      INTO l_statut_satellite
      FROM satellite
     WHERE id_satellite = :NEW.id_satellite;

    IF l_statut_satellite = 'Désorbité' THEN
        RAISE_APPLICATION_ERROR(-20005, 'Insertion refusée : le satellite est désorbité et ne peut plus etre affecte a une mission.');
    END IF;
END;
/
SHOW ERRORS TRIGGER trg_mission_terminee;


CREATE OR REPLACE TRIGGER trg_historique_statut
    AFTER UPDATE OF statut ON satellite
    FOR EACH ROW
    WHEN (OLD.statut <> NEW.statut)
BEGIN
    INSERT INTO historique_statut (
        id_satellite,
        ancien_statut,
        nouveau_statut,
        date_changement,
        motif
    ) VALUES (
        :OLD.id_satellite,
        :OLD.statut,
        :NEW.statut,
        SYSTIMESTAMP,
        'Mise a jour du statut de ' || :OLD.statut || ' vers ' || :NEW.statut
    );
END;
/
SHOW ERRORS TRIGGER trg_historique_statut;