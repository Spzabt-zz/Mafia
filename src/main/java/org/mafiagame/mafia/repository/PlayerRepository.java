package org.mafiagame.mafia.repository;

import org.mafiagame.mafia.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PlayerRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PlayerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addPlayer(Player player) {
        jdbcTemplate.update("insert into player (name, role, game_status) values (?, ?, ?);",
                player.getName(), player.getRole(), player.getGameStatus());
    }
}