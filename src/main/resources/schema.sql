CREATE DATABASE mafia_db;

CREATE TABLE "user"
(
    id          BIGSERIAL   NOT NULL PRIMARY KEY,
    name        VARCHAR(20) NOT NULL,
    role        VARCHAR(8)  NOT NULL,
    game_status BOOLEAN     NOT NULL
);

CREATE TABLE lobby
(
    id      BIGSERIAL NOT NULL PRIMARY KEY,
    user_id INTEGER   NOT NULL REFERENCES "user" (id)
);

CREATE TABLE server_statistics
(
    mafia_win_count  INTEGER NOT NULL,
    fair_win_count   INTEGER NOT NULL,
    total_game_count INTEGER NOT NULL
);