import { Router } from 'express';
import {
  getFenetres,
  getMissionStats,
  getOrbites,
  getSatelliteById,
  getSatelliteInstruments,
  getSatelliteMissions,
  getSatellites,
  getSatellitesOperationnels,
  getStations,
  getVolumesMensuels,
  validateFenetreRequest
} from './repositories/nanoOrbitRepository.js';

export const router = Router();

router.get('/health', (_request, response) => {
  response.json({ status: 'ok' });
});

router.get('/orbites', async (_request, response, next) => {
  try {
    response.json(await getOrbites());
  } catch (error) {
    next(error);
  }
});

router.get('/stations', async (_request, response, next) => {
  try {
    response.json(await getStations());
  } catch (error) {
    next(error);
  }
});

router.get('/satellites', async (_request, response, next) => {
  try {
    response.json(await getSatellites());
  } catch (error) {
    next(error);
  }
});

router.get('/satellites/operationnels', async (_request, response, next) => {
  try {
    response.json(await getSatellitesOperationnels());
  } catch (error) {
    next(error);
  }
});

router.get('/satellites/:id', async (request, response, next) => {
  try {
    const satellite = await getSatelliteById(request.params.id);
    if (!satellite) {
      response.status(404).json({
        message: `Satellite introuvable : ${request.params.id}`
      });
      return;
    }

    response.json(satellite);
  } catch (error) {
    next(error);
  }
});

router.get('/satellites/:id/instruments', async (request, response, next) => {
  try {
    response.json(await getSatelliteInstruments(request.params.id));
  } catch (error) {
    next(error);
  }
});

router.get('/satellites/:id/missions', async (request, response, next) => {
  try {
    response.json(await getSatelliteMissions(request.params.id));
  } catch (error) {
    next(error);
  }
});

router.get('/fenetres', async (_request, response, next) => {
  try {
    response.json(await getFenetres());
  } catch (error) {
    next(error);
  }
});

router.get('/missions/stats', async (_request, response, next) => {
  try {
    response.json(await getMissionStats());
  } catch (error) {
    next(error);
  }
});

router.get('/volumes/mensuels', async (_request, response, next) => {
  try {
    response.json(await getVolumesMensuels());
  } catch (error) {
    next(error);
  }
});

router.post('/fenetres/validate', async (request, response, next) => {
  try {
    const result = await validateFenetreRequest(request.body);
    response.status(result.isValid ? 200 : 422).json(result);
  } catch (error) {
    next(error);
  }
});
