DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'admin') THEN
      CREATE ROLE admin LOGIN PASSWORD 'admin';
   END IF;

   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'phoenix') THEN
      CREATE DATABASE phoenix OWNER admin;
   END IF;
END
$$;

GRANT ALL PRIVILEGES ON DATABASE phoenix TO admin;

-- Создание таблицы users, если она не существует
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Создание таблицы reports, если она не существует
CREATE TABLE IF NOT EXISTS reports (
    id SERIAL PRIMARY KEY,
    "userId" INTEGER NOT NULL REFERENCES users(id),
    "deviceId" VARCHAR(255),
    frequency VARCHAR(255) NOT NULL,
    mark VARCHAR(255) NOT NULL,
    screen VARCHAR(255) NOT NULL,
    body VARCHAR(255) NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    density REAL NOT NULL,
    ram BIGINT NOT NULL,
    "totalSpace" BIGINT NOT NULL,
    gyroscope VARCHAR(255),
    "versionOS" VARCHAR(255),
    "batteryState" INTEGER NOT NULL,
    level INTEGER NOT NULL,
    "dataStatus" INTEGER NOT NULL,
    gps BOOLEAN NOT NULL,
    bluetooth BOOLEAN NOT NULL,
    "audioReport" BOOLEAN NOT NULL
);

