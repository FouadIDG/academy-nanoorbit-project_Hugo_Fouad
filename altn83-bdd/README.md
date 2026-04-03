# ALTN83 - Bases de donnees reparties

> Module : ALTN83 - Semestre 8 - EFREI  
> SGBD cible : Oracle 23ai - schema `NO_ADMIN` sur `FREEPDB1`

## Organisation du module

| Phase | Theme | Points | Duree |
|---|---|---:|---:|
| 1 | Conception et architecture distribuee | 20 | 3h30 |
| 2 | Schema Oracle et triggers | 28 | 3h00 |
| 3 | PL/SQL et package `pkg_nanoOrbit` | 45 | 3h30 |
| 4 | Exploitation avancee et optimisation | 50 | 3h30 |

## Contenu du dossier

### `sujets/`
- enonces PDF du projet NanoOrbit

### `donnees/`
- fichiers CSV de reference fournis avec le sujet
- `07_AFFECTATION_STATION.csv` est utilise dans le jeu de donnees du modele actuel
### `scripts/`
- `01-Phase2_DDL.sql` : creation du schema Oracle
- `02-Phase2_DML.sql` : chargement du jeu de donnees initial
- `03-Phase2_Triggers.sql` : creation des triggers metier
- `04-Phase2_Controle.sql` : verifications schema + donnees + triggers

### Autres livrables
- `MCD.png` : schema graphique du MCD
- `CHANGELOG.md` : historique des changements du dossier

## MLD actuel

```text
ORBITE
  (id_orbite PK, type_orbite, altitude, inclinaison, periode_orbitale, excentricite, zone_couverture)

SATELLITE
  (id_satellite PK, nom_satellite, date_lancement, masse, format_cubesat, statut,
   duree_vie_prevue, capacite_batterie, #id_orbite)

HISTORIQUE_STATUT
  (id_historique PK, #id_satellite, ancien_statut, nouveau_statut, date_changement, motif)

INSTRUMENT
  (ref_instrument PK, type_instrument, modele, resolution, consommation, masse)

EMBARQUEMENT
  (#id_satellite PK FK, #ref_instrument PK FK, date_integration, etat_fonctionnement)

CENTRE_CONTROLE
  (id_centre PK, nom_centre, ville, region_geo, fuseau_horaire, statut)

STATION_SOL
  (code_station PK, nom_station, latitude, longitude, diametre_antenne, bande_frequence,
   debit_max, statut)

AFFECTATION_STATION
  (#id_centre PK FK, #code_station PK FK, date_affectation)

MISSION
  (id_mission PK, nom_mission, objectif, zone_geo_cible, date_debut, date_fin, statut_mission)

FENETRE_COM
  (id_fenetre PK, datetime_debut, duree, elevation_max, volume_donnees, statut,
   #id_satellite, #code_station)

PARTICIPATION
  (#id_satellite PK FK, #id_mission PK FK, role_satellite)
```

## Dictionnaire des donnees

### ORBITE

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `id_orbite` | `NUMBER` | Non | PK, identity |
| `type_orbite` | `VARCHAR2(10)` | Non | `LEO`, `MEO`, `SSO`, `GEO` |
| `altitude` | `NUMBER(5)` | Non | partie de l'unicite orbitale |
| `inclinaison` | `NUMBER(5,2)` | Non | partie de l'unicite orbitale |
| `periode_orbitale` | `NUMBER(6,2)` | Non | periode de revolution |
| `excentricite` | `NUMBER(6,4)` | Non | 0 proche d'une orbite circulaire |
| `zone_couverture` | `VARCHAR2(200)` | Non | zone geographique couverte |

### SATELLITE

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `id_satellite` | `VARCHAR2(20)` | Non | PK, format `SAT-XXX` |
| `nom_satellite` | `VARCHAR2(100)` | Non | nom operationnel |
| `date_lancement` | `DATE` | Non | date de mise en orbite |
| `masse` | `NUMBER(5,2)` | Non | masse en kg |
| `format_cubesat` | `VARCHAR2(5)` | Non | `1U`, `3U`, `6U`, `12U` |
| `statut` | `VARCHAR2(30)` | Non | `Opérationnel`, `En veille`, `Défaillant`, `Désorbité` |
| `duree_vie_prevue` | `NUMBER(4)` | Non | en mois |
| `capacite_batterie` | `NUMBER(6,1)` | Non | en Wh |
| `id_orbite` | `NUMBER` | Non | FK vers `ORBITE` |

### HISTORIQUE_STATUT

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `id_historique` | `NUMBER` | Non | PK, identity |
| `id_satellite` | `VARCHAR2(20)` | Non | FK vers `SATELLITE` |
| `ancien_statut` | `VARCHAR2(30)` | Non | meme domaine que `SATELLITE.statut` |
| `nouveau_statut` | `VARCHAR2(30)` | Non | meme domaine que `SATELLITE.statut` |
| `date_changement` | `TIMESTAMP` | Non | horodatage de la modification |
| `motif` | `VARCHAR2(500)` | Non | texte libre du changement |

### INSTRUMENT

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `ref_instrument` | `VARCHAR2(20)` | Non | PK |
| `type_instrument` | `VARCHAR2(50)` | Non | type metier |
| `modele` | `VARCHAR2(100)` | Non | designation commerciale |
| `resolution` | `NUMBER(6,1)` | Oui | nullable pour `INS-AIS-01` |
| `consommation` | `NUMBER(5,2)` | Non | en W |
| `masse` | `NUMBER(5,3)` | Non | en kg |

### EMBARQUEMENT

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `id_satellite` | `VARCHAR2(20)` | Non | PK/FK vers `SATELLITE` |
| `ref_instrument` | `VARCHAR2(20)` | Non | PK/FK vers `INSTRUMENT` |
| `date_integration` | `DATE` | Non | date d'integration sur le satellite |
| `etat_fonctionnement` | `VARCHAR2(20)` | Non | `Nominal`, `Dégradé`, `Hors service` |

### CENTRE_CONTROLE

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `id_centre` | `NUMBER` | Non | PK, identity |
| `nom_centre` | `VARCHAR2(100)` | Non | nom du centre |
| `ville` | `VARCHAR2(50)` | Non | ville du centre |
| `region_geo` | `VARCHAR2(50)` | Non | region de responsabilite |
| `fuseau_horaire` | `VARCHAR2(50)` | Non | fuseau IANA |
| `statut` | `VARCHAR2(20)` | Non | `Actif`, `Inactif` |

### STATION_SOL

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `code_station` | `VARCHAR2(20)` | Non | PK, format `GS-XXX-XX` |
| `nom_station` | `VARCHAR2(100)` | Non | nom de la station |
| `latitude` | `NUMBER(9,6)` | Non | coordonnee geographique |
| `longitude` | `NUMBER(9,6)` | Non | coordonnee geographique |
| `diametre_antenne` | `NUMBER(4,1)` | Non | diametre en m |
| `bande_frequence` | `VARCHAR2(10)` | Non | `UHF`, `S`, `X`, `Ka` |
| `debit_max` | `NUMBER(6,1)` | Non | en Mbps |
| `statut` | `VARCHAR2(20)` | Non | `Active`, `Maintenance`, `Inactive` |

### AFFECTATION_STATION

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `id_centre` | `NUMBER` | Non | PK/FK vers `CENTRE_CONTROLE` |
| `code_station` | `VARCHAR2(20)` | Non | PK/FK vers `STATION_SOL` |
| `date_affectation` | `DATE` | Non | date de rattachement de la station |

### MISSION

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `id_mission` | `VARCHAR2(20)` | Non | PK, format `MSN-XXX-AAAA` |
| `nom_mission` | `VARCHAR2(100)` | Non | nom fonctionnel |
| `objectif` | `VARCHAR2(500)` | Non | objectif scientifique |
| `zone_geo_cible` | `VARCHAR2(200)` | Non | zone cible |
| `date_debut` | `DATE` | Non | obligatoire |
| `date_fin` | `DATE` | Oui | nullable si mission active |
| `statut_mission` | `VARCHAR2(20)` | Non | `Active`, `Terminée` |

### FENETRE_COM

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `id_fenetre` | `NUMBER` | Non | PK, identity |
| `datetime_debut` | `TIMESTAMP` | Non | date/heure de debut |
| `duree` | `NUMBER(4)` | Non | contrainte `BETWEEN 1 AND 900` |
| `elevation_max` | `NUMBER(5,2)` | Non | angle maximal du passage |
| `volume_donnees` | `NUMBER(8,1)` | Oui | renseigne si `Réalisée` |
| `statut` | `VARCHAR2(20)` | Non | `Planifiée`, `Réalisée`, `Annulée` |
| `id_satellite` | `VARCHAR2(20)` | Non | FK vers `SATELLITE` |
| `code_station` | `VARCHAR2(20)` | Non | FK vers `STATION_SOL` |

### PARTICIPATION

| Attribut | Type Oracle | Null | Contraintes / remarques |
|---|---|---|---|
| `id_satellite` | `VARCHAR2(20)` | Non | PK/FK vers `SATELLITE` |
| `id_mission` | `VARCHAR2(20)` | Non | PK/FK vers `MISSION` |
| `role_satellite` | `VARCHAR2(100)` | Non | role du satellite dans la mission |

## Reponses aux questions du sujet

### Phase 1 - Architecture distribuee

**Q1. Quelles tables sont strictement locales a un centre de controle ?**  
Dans le modele actuel, aucune table n'est strictement locale au sens physique, car le schema Oracle reste centralise. En revanche, si on raisonne metier, les donnees les plus locales sont celles liees a l'exploitation d'un centre : `AFFECTATION_STATION` pour le rattachement station-centre, et `FENETRE_COM` car une fenetre depend d'une station geree par un centre. Les autres tables (`SATELLITE`, `ORBITE`, `MISSION`, `INSTRUMENT`) relevent d'un referentiel partage.

**Q2. Quelles tables doivent etre globales et comment les synchroniser ?**  
Les tables globales sont `SATELLITE`, `ORBITE`, `MISSION`, `INSTRUMENT`, `EMBARQUEMENT` et `PARTICIPATION`, car elles decrivent le referentiel metier commun a tous les centres. Le mecanisme conseille est une replication maitre-vers-sites ou une base centrale avec synchronisation en lecture sur les sites, afin d'eviter que plusieurs centres definissent des versions concurrentes d'un meme satellite, d'une meme mission ou d'une meme orbite.

**Q3. Comment Singapour continue a planifier si le serveur central est indisponible ?**  
Le plus robuste est de conserver localement les stations du centre, les fenetres recentes et une copie des referentiels globaux utiles a la planification. Une fragmentation horizontale de `FENETRE_COM` par centre ou par station, combinee a une replication locale des tables globales, permettrait a Singapour de continuer a preparer et enregistrer ses fenetres, puis de resynchroniser ensuite avec le serveur central.

**Q4. Quels risques de coherence existent en multi-sites ?**  
Deux risques principaux ressortent :
- deux centres peuvent planifier en meme temps des fenetres incompatibles pour un meme satellite si les donnees ne sont pas synchronisees instantanement ;
- un centre peut travailler avec un statut satellite obsolete, par exemple planifier une fenetre pour un satellite qui vient d'etre passe en `Désorbité` ailleurs.
Dans les deux cas, les triggers et la synchronisation inter-sites servent a limiter les conflits, mais ils ne remplacent pas une vraie strategie de replication et de resolution.

### Phase 2 - Questions DDL / contraintes

**Q1. Pourquoi ne peut-on pas creer `SATELLITE` avant `ORBITE` ?**  
Parce que `SATELLITE.id_orbite` est une cle etrangere vers `ORBITE.id_orbite`. L'entite `ORBITE` doit donc exister avant la creation de `SATELLITE`. Cela traduit la regle metier selon laquelle un satellite est place sur une orbite courante.

**Q2. La regle RG-S06 peut-elle etre verifiee en DDL seul ?**  
Non. Un `CHECK` ne peut pas verifier une information situee dans une autre table. Pour interdire une nouvelle fenetre ou une nouvelle participation pour un satellite `Désorbité`, il faut un mecanisme procedurale. C'est pour cela que cette regle est geree par les triggers `trg_valider_fenetre` et `trg_mission_terminee`.

**Q3. Comment implementer l'absence de chevauchement de fenetres ?**  
Cette contrainte compare une nouvelle fenetre avec d'autres lignes deja presentes dans `FENETRE_COM`. Elle n'est donc pas exprimable par un simple `CHECK`. Elle est implementee via `trg_no_chevauchement`, qui controle le recouvrement temporel pour le satellite et pour la station avant validation.

**Q4. Pourquoi `format_cubesat` est en `VARCHAR2(5)` ?**  
Parce que les valeurs attendues sont alphanumeriques (`1U`, `3U`, `6U`, `12U`). Un type numerique ferait perdre le suffixe `U` et compliquerait la validation. Le bon choix est donc `VARCHAR2(5)` avec un `CHECK` sur les valeurs autorisees.

## Triggers Phase 2

| Trigger | Evenement | Regle metier |
|---|---|---|
| `trg_valider_fenetre` | `BEFORE INSERT ON FENETRE_COM` | refuse un satellite desorbite ou une station en maintenance |
| `trg_no_chevauchement` | `INSERT OR UPDATE ON FENETRE_COM` | refuse les chevauchements pour un satellite ou une station |
| `trg_volume_realise` | `BEFORE INSERT OR UPDATE ON FENETRE_COM` | force `volume_donnees` a `NULL` si le statut n'est pas `Réalisée` |
| `trg_mission_terminee` | `BEFORE INSERT ON PARTICIPATION` | refuse une mission terminee et un satellite desorbite |
| `trg_historique_statut` | `AFTER UPDATE OF statut ON SATELLITE` | historise tout changement de statut |
