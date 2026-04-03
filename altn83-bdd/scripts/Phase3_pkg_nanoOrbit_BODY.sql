SET SERVEROUTPUT ON;

PROMPT ===== Creation du package pkg_nanoOrbit (BODY) =====

CREATE OR REPLACE PACKAGE BODY pkg_nanoOrbit AS

    FUNCTION mois_restants_satellite(
        p_id_satellite IN satellite.id_satellite%TYPE
    ) RETURN NUMBER IS
        l_date_lancement satellite.date_lancement%TYPE;
        l_duree_vie      satellite.duree_vie_prevue%TYPE;
    BEGIN
        SELECT date_lancement, duree_vie_prevue
          INTO l_date_lancement, l_duree_vie
          FROM satellite
         WHERE id_satellite = p_id_satellite;

        RETURN ROUND(l_duree_vie - MONTHS_BETWEEN(SYSDATE, l_date_lancement));
    END mois_restants_satellite;

    PROCEDURE planifier_fenetre(
        p_id_satellite   IN satellite.id_satellite%TYPE,
        p_code_station   IN station_sol.code_station%TYPE,
        p_datetime_debut IN fenetre_com.datetime_debut%TYPE,
        p_duree          IN fenetre_com.duree%TYPE,
        p_id_fenetre     OUT fenetre_com.id_fenetre%TYPE
    ) IS
        l_statut_satellite satellite.statut%TYPE;
        l_statut_station   station_sol.statut%TYPE;
        l_elevation_max    fenetre_com.elevation_max%TYPE := 60;
    BEGIN
        IF p_duree NOT BETWEEN 1 AND c_duree_max_fenetre THEN
            RAISE_APPLICATION_ERROR(-20101, 'La durée d''une fenêtre doit être comprise entre 1 et 900 secondes.');
        END IF;

        BEGIN
            SELECT statut
              INTO l_statut_satellite
              FROM satellite
             WHERE id_satellite = p_id_satellite;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20102, 'Satellite introuvable : ' || p_id_satellite);
        END;

        IF l_statut_satellite <> 'Opérationnel' THEN
            RAISE_APPLICATION_ERROR(-20103, 'Le satellite doit être Opérationnel pour planifier une fenêtre.');
        END IF;

        BEGIN
            SELECT statut
              INTO l_statut_station
              FROM station_sol
             WHERE code_station = p_code_station;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20104, 'Station introuvable : ' || p_code_station);
        END;

        IF l_statut_station <> 'Active' THEN
            RAISE_APPLICATION_ERROR(-20105, 'La station doit être Active pour planifier une fenêtre.');
        END IF;

        INSERT INTO fenetre_com (
            datetime_debut,
            duree,
            elevation_max,
            volume_donnees,
            statut,
            id_satellite,
            code_station
        ) VALUES (
            p_datetime_debut,
            p_duree,
            l_elevation_max,
            NULL,
            c_statut_min_fenetre,
            p_id_satellite,
            p_code_station
        )
        RETURNING id_fenetre INTO p_id_fenetre;
    END planifier_fenetre;

    PROCEDURE cloturer_fenetre(
        p_id_fenetre      IN fenetre_com.id_fenetre%TYPE,
        p_volume_donnees  IN fenetre_com.volume_donnees%TYPE
    ) IS
        l_statut_courant    fenetre_com.statut%TYPE;
        l_volume_theorique  NUMBER;
    BEGIN
        IF p_volume_donnees IS NULL OR p_volume_donnees < 0 THEN
            RAISE_APPLICATION_ERROR(-20106, 'Le volume de données doit être renseigné et positif.');
        END IF;

        BEGIN
            SELECT statut
              INTO l_statut_courant
              FROM fenetre_com
             WHERE id_fenetre = p_id_fenetre
             FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20107, 'Fenêtre introuvable : ' || p_id_fenetre);
        END;

        IF l_statut_courant = 'Réalisée' THEN
            RAISE_APPLICATION_ERROR(-20108, 'La fenêtre ' || p_id_fenetre || ' est déjà clôturée.');
        END IF;

        l_volume_theorique := calculer_volume_theorique(p_id_fenetre);

        IF p_volume_donnees > l_volume_theorique THEN
            RAISE_APPLICATION_ERROR(
                -20109,
                'Le volume réalisé ne peut pas dépasser le volume théorique (' || l_volume_theorique || ').'
            );
        END IF;

        UPDATE fenetre_com
           SET statut = 'Réalisée',
               volume_donnees = p_volume_donnees
         WHERE id_fenetre = p_id_fenetre;
    END cloturer_fenetre;

    PROCEDURE affecter_satellite_mission(
        p_id_satellite  IN participation.id_satellite%TYPE,
        p_id_mission    IN participation.id_mission%TYPE,
        p_role          IN participation.role_satellite%TYPE
    ) IS
        l_statut_satellite satellite.statut%TYPE;
        l_statut_mission   mission.statut_mission%TYPE;
    BEGIN
        IF TRIM(p_role) IS NULL THEN
            RAISE_APPLICATION_ERROR(-20110, 'Le rôle de participation est obligatoire.');
        END IF;

        BEGIN
            SELECT statut
              INTO l_statut_satellite
              FROM satellite
             WHERE id_satellite = p_id_satellite;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20111, 'Satellite introuvable : ' || p_id_satellite);
        END;

        IF l_statut_satellite = 'Désorbité' THEN
            RAISE_APPLICATION_ERROR(-20112, 'Le satellite est désorbité et ne peut pas être affecté.');
        END IF;

        BEGIN
            SELECT statut_mission
              INTO l_statut_mission
              FROM mission
             WHERE id_mission = p_id_mission;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20113, 'Mission introuvable : ' || p_id_mission);
        END;

        IF l_statut_mission <> 'Active' THEN
            RAISE_APPLICATION_ERROR(-20114, 'La mission doit être Active pour accepter une affectation.');
        END IF;

        INSERT INTO participation (
            id_satellite,
            id_mission,
            role_satellite
        ) VALUES (
            p_id_satellite,
            p_id_mission,
            p_role
        );
    EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN
            RAISE_APPLICATION_ERROR(-20115, 'Cette participation existe déjà.');
    END affecter_satellite_mission;

    PROCEDURE mettre_en_revision(
        p_id_satellite IN satellite.id_satellite%TYPE
    ) IS
        l_statut_courant satellite.statut%TYPE;
        l_mois_restants  NUMBER;
    BEGIN
        BEGIN
            SELECT statut
              INTO l_statut_courant
              FROM satellite
             WHERE id_satellite = p_id_satellite
             FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                RAISE_APPLICATION_ERROR(-20116, 'Satellite introuvable : ' || p_id_satellite);
        END;

        IF l_statut_courant = 'Désorbité' THEN
            RAISE_APPLICATION_ERROR(-20117, 'Un satellite désorbité ne peut pas être remis en révision.');
        END IF;

        IF l_statut_courant = 'En veille' THEN
            RAISE_APPLICATION_ERROR(-20118, 'Le satellite est déjà en veille.');
        END IF;

        l_mois_restants := mois_restants_satellite(p_id_satellite);

        IF l_statut_courant <> 'Défaillant' AND l_mois_restants > c_seuil_revision THEN
            RAISE_APPLICATION_ERROR(
                -20119,
                'La révision n''est autorisée qu''en fin de vie (' || c_seuil_revision || ' mois) ou après défaillance.'
            );
        END IF;

        UPDATE satellite
           SET statut = 'En veille'
         WHERE id_satellite = p_id_satellite;
    END mettre_en_revision;

    FUNCTION calculer_volume_theorique(
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
            RAISE_APPLICATION_ERROR(-20120, 'Fenêtre introuvable : ' || p_id_fenetre);
    END calculer_volume_theorique;

    FUNCTION statut_constellation
    RETURN VARCHAR2 IS
        l_nb_operationnels NUMBER;
        l_nb_veille        NUMBER;
        l_nb_defaillants   NUMBER;
        l_nb_desorbites    NUMBER;
        l_libelle          VARCHAR2(50);
    BEGIN
        SELECT SUM(CASE WHEN statut = 'Opérationnel' THEN 1 ELSE 0 END),
               SUM(CASE WHEN statut = 'En veille' THEN 1 ELSE 0 END),
               SUM(CASE WHEN statut = 'Défaillant' THEN 1 ELSE 0 END),
               SUM(CASE WHEN statut = 'Désorbité' THEN 1 ELSE 0 END)
          INTO l_nb_operationnels, l_nb_veille, l_nb_defaillants, l_nb_desorbites
          FROM satellite;

        IF l_nb_operationnels = 0 THEN
            l_libelle := 'Critique';
        ELSIF l_nb_defaillants > 0 THEN
            l_libelle := 'Dégradée';
        ELSIF l_nb_operationnels >= 3 THEN
            l_libelle := 'Nominale';
        ELSE
            l_libelle := 'Sous surveillance';
        END IF;

        RETURN l_libelle
               || ' (Opérationnels=' || l_nb_operationnels
               || ', En veille=' || l_nb_veille
               || ', Défaillants=' || l_nb_defaillants
               || ', Désorbites=' || l_nb_desorbites
               || ')';
    END statut_constellation;

    FUNCTION stats_satellite(
        p_id_satellite IN satellite.id_satellite%TYPE
    ) RETURN t_stats_satellite IS
        l_stats t_stats_satellite;
        l_exists NUMBER;
    BEGIN
        SELECT COUNT(*)
          INTO l_exists
          FROM satellite
         WHERE id_satellite = p_id_satellite;

        IF l_exists = 0 THEN
            RAISE_APPLICATION_ERROR(-20121, 'Satellite introuvable : ' || p_id_satellite);
        END IF;

        SELECT COUNT(*),
               NVL(SUM(volume_donnees), 0),
               NVL(ROUND(AVG(duree), 2), 0)
          INTO l_stats.nb_fenetres,
               l_stats.volume_total,
               l_stats.duree_moy_secondes
          FROM fenetre_com
         WHERE id_satellite = p_id_satellite;

        RETURN l_stats;
    END stats_satellite;

END pkg_nanoOrbit;
/
SHOW ERRORS PACKAGE BODY pkg_nanoOrbit;

PROMPT ===== Fin creation du package pkg_nanoOrbit (BODY) =====
