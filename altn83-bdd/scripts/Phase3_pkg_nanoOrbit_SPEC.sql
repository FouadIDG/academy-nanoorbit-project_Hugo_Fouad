SET SERVEROUTPUT ON;

PROMPT ===== Creation du package pkg_nanoOrbit (SPEC) =====

CREATE OR REPLACE PACKAGE pkg_nanoOrbit AS

    c_statut_min_fenetre CONSTANT fenetre_com.statut%TYPE := 'Planifiée';
    c_duree_max_fenetre CONSTANT PLS_INTEGER := 900;
    c_seuil_revision CONSTANT PLS_INTEGER := 12;

    TYPE t_stats_satellite IS RECORD (
        nb_fenetres          NUMBER,
        volume_total         NUMBER,
        duree_moy_secondes   NUMBER
    );

    PROCEDURE planifier_fenetre(
        p_id_satellite   IN satellite.id_satellite%TYPE,
        p_code_station   IN station_sol.code_station%TYPE,
        p_datetime_debut IN fenetre_com.datetime_debut%TYPE,
        p_duree          IN fenetre_com.duree%TYPE,
        p_id_fenetre     OUT fenetre_com.id_fenetre%TYPE
    );

    PROCEDURE cloturer_fenetre(
        p_id_fenetre      IN fenetre_com.id_fenetre%TYPE,
        p_volume_donnees  IN fenetre_com.volume_donnees%TYPE
    );

    PROCEDURE affecter_satellite_mission(
        p_id_satellite  IN participation.id_satellite%TYPE,
        p_id_mission    IN participation.id_mission%TYPE,
        p_role          IN participation.role_satellite%TYPE
    );

    PROCEDURE mettre_en_revision(
        p_id_satellite IN satellite.id_satellite%TYPE
    );

    FUNCTION calculer_volume_theorique(
        p_id_fenetre IN fenetre_com.id_fenetre%TYPE
    ) RETURN NUMBER;

    FUNCTION statut_constellation
    RETURN VARCHAR2;

    FUNCTION stats_satellite(
        p_id_satellite IN satellite.id_satellite%TYPE
    ) RETURN t_stats_satellite;

END pkg_nanoOrbit;
/
SHOW ERRORS PACKAGE pkg_nanoOrbit;

PROMPT ===== Fin creation du package pkg_nanoOrbit (SPEC) =====
