# NanoOrbit Ground Control

## Lancer le projet

### 1. Lancer la base de donnees Oracle
Depuis la racine du projet :

```bash
cd altn83-bdd
docker compose up -d
```

Le conteneur Oracle demarre sur `localhost:1521` et initialise automatiquement le schema `NO_ADMIN` avec les scripts SQL via le bootstrap :
- `01-Phase2_DDL.sql`
- `02-Phase2_DML.sql`
- `03-Phase2_Triggers.sql`
- `07-Phase3_pkg_nanoOrbit_SPEC.sql`
- `06-Phase3_pkg_nanoOrbit_BODY.sql`
- `09-Phase4_Exploitation_Avancee.sql`
- `04-Phase2_Controle.sql`

Le bootstrap est monte dans le conteneur et s'execute au demarrage de la base.

Si besoin, on peut verifier les logs :

```bash
docker logs -f NanoOrbit_oracle_23ai
```

### 2. Lancer l'API Node.js
Depuis la racine du projet :

```bash
cd api
npm install
npm run start
```

L'API ecoute sur `http://localhost:3000`.

### 3. Lancer l'application Android
Ouvrir le projet suivant dans Android Studio :

```text
altn82-android/starter
```

Ensuite :
1. lancer un emulateur Android
2. cliquer sur `Run`
3. attendre le chargement du dashboard

L'application Android pointe sur `http://10.0.2.2:3000/`, donc l'API doit tourner sur la machine hote.

## Provoquer les trois modes de l'application

### 1. Mode en ligne
Conditions :
- base de donnees allumee
- API allumee
- application ouverte

Procedure :
1. lancer `docker compose up -d` dans `altn83-bdd`
2. lancer `npm run start` dans `api`
3. ouvrir l'application
4. faire un refresh

Resultat attendu :
- pas de banniere `Mode hors-ligne`
- pas de banniere `Mode demonstration`
- les donnees viennent de l'API

### 2. Mode hors-ligne
Conditions :
- il faut d'abord avoir charge l'application au moins une fois avec l'API disponible
- cela remplit le cache local Room

Procedure :
1. lancer la base et l'API
2. ouvrir l'application
3. faire un refresh sur le dashboard ou le planning
4. fermer l'API
5. relancer l'application ou refaire un refresh

Resultat attendu :
- banniere `Mode hors-ligne`
- message du type `Mis a jour il y a ...`
- les donnees viennent de Room

### 3. Mode MockData
Conditions :
- l'API doit etre indisponible
- le stockage de l'application doit etre vide

Important :
pour supprimer Room, il ne faut pas seulement vider le cache Android.  
Il faut effacer les **donnees de l'application** (`Clear storage` / `Effacer les donnees`), sinon la base Room reste presente.

Procedure :
1. couper l'API
2. effacer les donnees de l'application
3. relancer l'application

Resultat attendu :
- banniere `Mode demonstration`
- les donnees viennent de `MockData`

## Comment effacer les donnees de l'application

### Depuis l'emulateur Android
1. ouvrir `Settings`
2. aller dans `Apps`
3. choisir `NanoOrbit`
4. ouvrir `Storage & cache`
5. cliquer sur `Clear storage` ou `Effacer les donnees`

## Resume rapide

### Mode en ligne
- base ON
- API ON
- app ouverte

### Mode hors-ligne
- base ON ou OFF
- API OFF
- Room deja rempli

### Mode MockData
- API OFF
- Room vide
- donnees de l'application effacees
