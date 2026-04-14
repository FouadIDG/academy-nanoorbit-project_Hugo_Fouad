# ALTN82 - NanoOrbit Ground Control

## Reponses aux questions du sujet

### Phase 1 - Q1
On utilise `LazyColumn` au lieu de `Column` parce que `LazyColumn` n'affiche et ne compose que les elements visibles a l'ecran. C'est plus adapte a une liste potentiellement longue comme une flotte de satellites.  
Avec `Column`, si on avait 100 satellites, Android essaierait de tout composer d'un coup. Cela consommerait plus de memoire, augmenterait le temps de rendu initial et pourrait provoquer des ralentissements au scroll.

### Phase 1 - Q2
Une `enum class` Kotlin est preferable a une `String` libre parce qu'elle limite les valeurs possibles a celles qui sont autorisees par le modele metier.  
Par exemple, avec une `String`, on pourrait avoir des erreurs comme `"operationnel"`, `"Opé"`, `"OPERATIONNEL"` ou une faute de frappe. Avec une enum, on garde des valeurs fixes, ce qui rend le code plus sur, plus lisible et coherent avec le `CHECK` Oracle.

### Phase 1 - Q3
L'application peut empecher la planification d'une fenetre pour un satellite desorbite en desactivant l'action de clic ou le bouton de validation si le statut est `DESORBITE`. On peut aussi griser la carte et afficher un message explicite pour prevenir l'utilisateur.  
Cela correspond au meme principe que le trigger Oracle T1 : cote Android on bloque l'action dans l'interface, et cote base de donnees le trigger sert de securite finale si jamais une requete invalide est quand meme envoyee.

## Synergie avec ALTN83

### 1. Modeles de donnees
Les data classes Kotlin reprennent la structure du MLD Oracle. Par exemple, `Satellite` correspond a la table `SATELLITE`, `FenetreCom` a `FENETRE_COM`, `StationSol` a `STATION_SOL`, etc.  
L'idee est d'avoir un miroir simple entre la base et l'application mobile pour eviter les incoherences de nommage, de type ou de regles metier.

### 2. Regle RG-F04
La regle RG-F04 impose qu'une fenetre de communication ait une duree comprise entre 1 et 900 secondes. Cette verification est faite cote Android avant validation, avec un message d'erreur lisible si la valeur depasse la borne.  
Cela reprend la meme logique que dans ALTN83, ou la contrainte est defendue par les traitements Oracle. Donc on a une double protection : une validation utilisateur dans l'app, puis une validation technique en base.

### 3. Hors-ligne / disponibilite reseau
Si l'API est disponible, l'application utilise les donnees du serveur. Si l'API ne repond plus, elle peut basculer sur les donnees locales.  
La strategie retenue est la suivante :
- API accessible : donnees fraiches, pas de banniere hors-ligne
- API indisponible + cache Room present : mode hors-ligne avec banniere
- API indisponible + cache vide : repli sur `MockData`

Je trouve que cette logique est coherente avec la question ALTN83 sur la continuite de service : meme si le serveur central n'est plus joignable, l'operateur peut encore consulter des informations locales et continuer une partie de l'activite.

## Choix d'implementation

### Architecture
Le projet est organise avec une architecture MVVM :
- les modeles contiennent les data classes et enums
- le repository gere l'acces aux donnees
- le ViewModel gere l'etat et la logique de filtrage
- les ecrans Compose affichent les donnees

Ce choix permet d'eviter de melanger reseau, logique metier et interface dans les memes fichiers.

### Gestion d'etat
Les ecrans observent les `StateFlow` du ViewModel. Cela permet d'avoir une interface reactive : quand les donnees changent, l'ecran se met a jour automatiquement.

### Hors-ligne
Le mode hors-ligne repose sur Room pour conserver les dernieres donnees synchronisees. En cas de perte reseau, l'application peut continuer a afficher les satellites et les fenetres deja recuperes.  
Si aucune donnee locale n'est disponible, `MockData` sert de solution de secours pour la demonstration.

### Notifications
Les notifications locales sont gerees avec `WorkManager`. Le worker regarde les fenetres planifiees proches dans Room et envoie une notification locale si un passage approche.  
Ce mecanisme est pratique pour un projet scolaire, meme s'il ne remplace pas un vrai systeme de push serveur comme on le ferait dans une application industrielle.