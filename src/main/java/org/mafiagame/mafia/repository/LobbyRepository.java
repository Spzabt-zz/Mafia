package org.mafiagame.mafia.repository;

import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LobbyRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LobbyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(Lobby lobby) {
        jdbcTemplate.update("insert into lobby (number, game_status) values (?, ?)",
                lobby.getNumber(), lobby.getGameStatus());
    }

   /* public List<Lobby> lobbies() {
        List<Player> players = jdbcTemplate.query("SELECT * FROM lobby", new BeanPropertyRowMapper<>(Player.class));
        return players;
    }*/

    /*public void delete(int id) {
        jdbcTemplate.update("DELETE FROM lobby WHERE id=?", id);
    }*/
}
