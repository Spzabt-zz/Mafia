CREATE DATABASE mafia_db;

DROP DATABASE mafia_db;

DROP TABLE player CASCADE;
DROP TABLE server_statistics;
DROP TABLE lobby CASCADE;

TRUNCATE TABLE player RESTART IDENTITY;
TRUNCATE TABLE lobby RESTART IDENTITY CASCADE;

CREATE TABLE lobby
(
    id          BIGSERIAL   NOT NULL PRIMARY KEY,
    name        VARCHAR(50),
    number      INTEGER     NOT NULL,
    game_status VARCHAR(50) NOT NULL
);

CREATE TABLE player
(
    id        BIGSERIAL NOT NULL PRIMARY KEY,
    name      VARCHAR(20),
    role      VARCHAR(8),
    alive     BOOLEAN,
    position  INTEGER,
    candidate BOOLEAN,
    vote      INTEGER,
    admin     BOOLEAN,
    lobby_id  INTEGER   NOT NULL REFERENCES lobby (id)
);

CREATE TABLE votes
(
    player_id    INTEGER NOT NULL REFERENCES player (id),
    candidate_id INTEGER,
    day          INTEGER
);

CREATE TABLE server_statistics
(
    mafia_win_count  INTEGER NOT NULL,
    fair_win_count   INTEGER NOT NULL,
    total_game_count INTEGER NOT NULL
);


SELECT lb.*
FROM lobby lb
WHERE lb.number = 523141;


SELECT lb.*
FROM lobby lb
         INNER JOIN player p on lb.id = 16
LIMIT 1;


SELECT pl.*
FROM player pl
WHERE pl.lobby_id = 22
  AND pl.name = 'fromLobby150420-1';