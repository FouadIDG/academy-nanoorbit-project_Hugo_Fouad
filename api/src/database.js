import oracledb from 'oracledb';
import { config } from './config.js';

oracledb.outFormat = oracledb.OUT_FORMAT_OBJECT;

let pool;

export async function initializePool() {
  if (pool) {
    return pool;
  }

  pool = await oracledb.createPool({
    user: config.db.user,
    password: config.db.password,
    connectString: config.db.connectString,
    poolMin: config.db.poolMin,
    poolMax: config.db.poolMax,
    poolIncrement: config.db.poolIncrement
  });

  return pool;
}

export async function executeQuery(sql, binds = {}, options = {}) {
  const connection = await pool.getConnection();

  try {
    const result = await connection.execute(sql, binds, {
      autoCommit: true,
      ...options
    });
    return result;
  } finally {
    await connection.close();
  }
}

export async function closePool() {
  if (!pool) {
    return;
  }

  await pool.close(10);
  pool = undefined;
}
