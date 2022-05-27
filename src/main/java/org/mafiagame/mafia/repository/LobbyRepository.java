package org.mafiagame.mafia.repository;

import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.enam.GameStatus;
import org.mafiagame.mafia.repository.mapper.LobbyRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
        jdbcTemplate.update("insert into lobby (name, number, game_status) values (?, ?, ?)",
                lobby.getName(), lobby.getNumber(), lobby.getGameStatus());
    }

    public List<Lobby> lobbies() {
        return jdbcTemplate.query("SELECT * FROM lobby", new LobbyRowMapper());
    }

    public Lobby selectCurrentLobbyByNumber(int number) {
        return jdbcTemplate.queryForObject("SELECT lb.* FROM lobby lb WHERE lb.number = ?",
                new LobbyRowMapper(), number);
    }

    public Lobby selectCurrentLobbyByPlayerLobbyId(int lobbyId) {
        return jdbcTemplate.queryForObject("SELECT lb.* FROM lobby lb INNER JOIN player p on lb.id = ? LIMIT 1",
                new LobbyRowMapper(), lobbyId);
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM lobby WHERE id=?", id);
    }

    public void updateLobbyStatus(int number) {
        jdbcTemplate.update("UPDATE lobby SET game_status = ? WHERE number = ?", GameStatus.IN_PROGRESS.toString(), number);
    }
}
