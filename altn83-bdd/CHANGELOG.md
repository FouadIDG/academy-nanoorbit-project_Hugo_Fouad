# CHANGELOG

## 2026-04-01

### Phase 1 - Conception et modélisation
- ajout du livrable graphique MCD.png
- MCD construit a partir du cahier des charges NanoOrbit et des regles de gestion de la phase 1
- modelisation des associations porteuses d'attributs :
  - `EMBARQUEMENT`
  - `PARTICIPATION`
  - `AFFECTATION_STATION`
  - `FENETRE_COM`
- prise en compte des points sensibles de la phase 1 :
  - rattachement d'une station a un seul centre de controle
  - distinction entre contraintes structurelles et contraintes procedurales
  - identification des regles a sortir du MCD pour implementation en triggers

### Phase 2 - Schéma Oracle
- ajout du script Phase2_DDL
- creation des 11 tables du schema NanoOrbit :
  - `ORBITE`
  - `SATELLITE`
  - `HISTORIQUE_STATUT`
  - `INSTRUMENT`
  - `EMBARQUEMENT`
  - `CENTRE_CONTROLE`
  - `STATION_SOL`
  - `AFFECTATION_STATION`
  - `MISSION`
  - `FENETRE_COM`
  - `PARTICIPATION`
- ajout des contraintes `PK`, `FK`, `CHECK`, `UNIQUE`
- nettoyage du schema via `DROP TABLE IF EXISTS` au debut du script

### Phase 2 - Jeu de donnees initial
- ajout du script Phase2_DML
- chargement du jeu de donnees initial en suivant le PDF de reference
- choix retenus pour le DML :
  - 2 centres seulement au chargement initial : Paris et Houston
  - `GS-SGP-01` rattachee a Houston
  - pas d'insertion manuelle dans `HISTORIQUE_STATUT`

### Phase 2 - Triggers
- ajout du script Phase2_Triggers
- creation des 5 triggers attendus :
  - `trg_valider_fenetre`
  - `trg_no_chevauchement`
  - `trg_volume_realise`
  - `trg_mission_terminee`
  - `trg_historique_statut`
- `trg_no_chevauchement` est implemente en compound trigger pour eviter le probleme Oracle de table mutante
- `trg_mission_terminee` couvre aussi le cas du satellite desorbite pour respecter `RG-S06`

### Phase 2 - Controles
- mise a jour du script Phase2_Controle
- verifications ajoutees :
  - utilisateur courant
  - tables presentes
  - contraintes nommees
  - triggers presents et leur statut
  - erreurs de compilation des triggers
  - comptage des lignes par table
  - repartition des statuts sur `SATELLITE`, `STATION_SOL` et `MISSION`

### Validation locale
- execution du DDL sur le schema `NO_ADMIN`
- execution du DML sur le schema `NO_ADMIN`
- execution du script des triggers sur le schema `NO_ADMIN`
- verification des objets via le script de controle
- validation comportementale des 5 triggers :
  - refus satellite desorbite sur `FENETRE_COM`
  - refus station en maintenance sur `FENETRE_COM`
  - refus de chevauchement de fenetres
  - remise a `NULL` automatique de `volume_donnees` si statut non `Réalisée`
  - refus mission terminee
  - refus affectation d'un satellite desorbite a une mission active
  - insertion dans `HISTORIQUE_STATUT` sur changement de statut
