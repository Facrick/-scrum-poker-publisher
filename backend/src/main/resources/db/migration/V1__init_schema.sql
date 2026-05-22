-- Создаем схему, если она не существует
CREATE SCHEMA IF NOT EXISTS scrum_poker;

-- Таблица пользователей (модераторов)
CREATE TABLE IF NOT EXISTS scrum_poker.users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Таблица комнат
CREATE TABLE IF NOT EXISTS scrum_poker.rooms (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    current_round_id UUID,
    status VARCHAR(50),
    created_at TIMESTAMP,
    settings_json TEXT
);

-- Таблица участников
CREATE TABLE IF NOT EXISTS scrum_poker.participants (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    connected BOOLEAN,
    joined_at TIMESTAMP
);

-- Таблица голосов
CREATE TABLE IF NOT EXISTS scrum_poker.votes (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL,
    round_id UUID NOT NULL,
    participant_id UUID NOT NULL,
    vote_value VARCHAR(20) NOT NULL -- ИСПРАВЛЕНО: value -> vote_value и длина
);
