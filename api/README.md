# NanoOrbit API

## Demarrage

```bash
cd api
cp .env.example .env
npm install
npm run dev
```

## Configuration Oracle

Par defaut l'API se connecte a `NO_ADMIN / Admin_NO_2026` sur `localhost:1521/FREEPDB1`,
ce qui correspond au conteneur `altn83-bdd/docker-compose.yml`.

Avant de lancer les endpoints enrichis, execute aussi
`altn83-bdd/scripts/09-Phase4_Exploitation_Avancee.sql` pour creer
`v_satellites_operationnels`, `v_fenetres_detail`, `v_stats_missions`
et `mv_volumes_mensuels`.

## Endpoints

- `GET /health`
- `GET /orbites`
- `GET /stations`
- `GET /satellites`
- `GET /satellites/operationnels`
- `GET /satellites/:id`
- `GET /satellites/:id/instruments`
- `GET /satellites/:id/missions`
- `GET /fenetres`
- `GET /missions/stats`
- `GET /volumes/mensuels`
- `POST /fenetres/validate`
- `POST /fenetres`

## Validation d'une fenetre

```bash
curl -X POST http://localhost:3000/fenetres/validate \
  -H "Content-Type: application/json" \
  -d '{"satelliteId":"SAT-001","codeStation":"GS-KIR-01","dureeSecondes":300}'
```

## Planification d'une fenetre

```bash
curl -X POST http://localhost:3000/fenetres \
  -H "Content-Type: application/json" \
  -d '{"satelliteId":"SAT-001","codeStation":"GS-KIR-01","datetimeDebut":"2026-04-28T14:30","dureeSecondes":300,"elevationMaxDegres":60}'
```
