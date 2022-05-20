CREATE DATABASE mafia_db;

CREATE TABLE player
(
    id   BIGSERIAL   NOT NULL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    role VARCHAR(8)  NOT NULL
);

DROP DATABASE mafia_db;
DROP TABLE player;
DROP TABLE server_statistics;
TRUNCATE TABLE player;

SELECT *
FROM player;

--insert into player (name, role) values ('dwad', 'mafia');

CREATE TABLE lobby
(
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    number      INTEGER   NOT NULL,
    game_status BOOLEAN   NOT NULL
);

CREATE TABLE player
(
    id        BIGSERIAL   NOT NULL PRIMARY KEY,
    name      VARCHAR(20) NOT NULL,
    role      VARCHAR(8)  NOT NULL,
    alive     BOOLEAN     NOT NULL,
    position  INTEGER     NOT NULL,
    candidate BOOLEAN     NOT NULL,
    vote      INTEGER,
    admin     BOOLEAN     NOT NULL,
    lobby_id  INTEGER     NOT NULL REFERENCES lobby (id)
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