import dotenv from 'dotenv';

dotenv.config();

export const config = {
  port: Number(process.env.PORT || 3000),
  db: {
    user: process.env.DB_USER || 'NO_ADMIN',
    password: process.env.DB_PASSWORD || 'Admin_NO_2026',
    connectString: process.env.DB_CONNECT_STRING || 'localhost:1521/FREEPDB1',
    poolMin: Number(process.env.DB_POOL_MIN || 1),
    poolMax: Number(process.env.DB_POOL_MAX || 5),
    poolIncrement: Number(process.env.DB_POOL_INCREMENT || 1)
  }
};
