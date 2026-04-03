SET SERVEROUTPUT ON;

PROMPT ===== Validation du package pkg_nanoOrbit =====

DECLARE
    l_id_fenetre     fenetre_com.id_fenetre%TYPE;
    l_volume_theo    NUMBER;
    l_stats          pkg_nanoOrbit.t_stats_satellite;
BEGIN
    SAVEPOINT phase3_pkg_validation;

    pkg_nanoOrbit.planifier_fenetre(
        p_id_satellite   => 'SAT-001',
        p_code_station   => 'GS-KIR-01',
        p_datetime_debut => TO_TIMESTAMP('2024-01-22 14:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        p_duree          => 240,
        p_id_fenetre     => l_id_fenetre
    );
    DBMS_OUTPUT.PUT_LINE('Fenetre planifiee : id=' || l_id_fenetre);

    l_volume_theo := pkg_nanoOrbit.calculer_volume_theorique(l_id_fenetre);
    DBMS_OUTPUT.PUT_LINE('Volume theorique = ' || l_volume_theo);

    pkg_nanoOrbit.cloturer_fenetre(
        p_id_fenetre     => l_id_fenetre,
        p_volume_donnees => 11500
    );
    DBMS_OUTPUT.PUT_LINE('Fenetre cloturee : id=' || l_id_fenetre);

    pkg_nanoOrbit.affecter_satellite_mission(
        p_id_satellite => 'SAT-004',
        p_id_mission   => 'MSN-ARC-2023',
        p_role         => 'Satellite de relais'
    );
    DBMS_OUTPUT.PUT_LINE('Participation ajoutee : SAT-004 -> MSN-ARC-2023');

    pkg_nanoOrbit.mettre_en_revision('SAT-001');
    DBMS_OUTPUT.PUT_LINE('SAT-001 passe en revision.');

    l_stats := pkg_nanoOrbit.stats_satellite('SAT-001');
    DBMS_OUTPUT.PUT_LINE(
        'Stats SAT-001 -> fenetres=' || l_stats.nb_fenetres
        || ', volume=' || l_stats.volume_total
        || ', duree moyenne=' || l_stats.duree_moy_secondes
    );

    DBMS_OUTPUT.PUT_LINE('Statut constellation -> ' || pkg_nanoOrbit.statut_constellation());

    ROLLBACK TO phase3_pkg_validation;
    DBMS_OUTPUT.PUT_LINE('Validation terminee avec rollback du jeu de test.');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK TO phase3_pkg_validation;
        DBMS_OUTPUT.PUT_LINE('Validation en erreur : ' || SQLERRM);
        RAISE;
END;
/

PROMPT ===== Fin validation du package pkg_nanoOrbit =====
