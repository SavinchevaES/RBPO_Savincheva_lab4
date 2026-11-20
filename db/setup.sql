-- Шаг 1. Выполнить в pgAdmin под postgres, в базе postgres (КАЖДУЮ команду отдельно):
CREATE USER volunteer_app WITH PASSWORD 'ваш_пароль';
CREATE DATABASE volunteer_db;

-- Шаг 2. Выполнить в базе volunteer_db (Query Tool именно этой базы):
GRANT CONNECT ON DATABASE volunteer_db TO volunteer_app;
GRANT USAGE, CREATE ON SCHEMA public TO volunteer_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO volunteer_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO volunteer_app;

-- Таблица refresh-сессий для задания 5. Её можно создать до первого запуска приложения.
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    device_id VARCHAR(255),
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL UNIQUE,
    access_token_expiry TIMESTAMP WITH TIME ZONE NOT NULL,
    refresh_token_expiry TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(10) NOT NULL
);
