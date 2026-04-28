import cors from 'cors';
import express from 'express';
import { config } from './config.js';
import { closePool, initializePool } from './database.js';
import { router } from './routes.js';

const app = express();

app.use(cors());
app.use(express.json());
app.use(router);

app.use((error, _request, response, _next) => {
  console.error(error);
  const statusCode = error.statusCode || 500;
  response.status(statusCode).json({
    message: statusCode >= 500 ? 'Erreur interne API NanoOrbit' : error.message,
    details: statusCode >= 500 ? error.message : undefined
  });
});

await initializePool();

const server = app.listen(config.port, () => {
  console.log(`NanoOrbit API listening on http://localhost:${config.port}`);
});

async function shutdown() {
  server.close(async () => {
    await closePool();
    process.exit(0);
  });
}

process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);
