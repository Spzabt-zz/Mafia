package org.mafiagame.mafia.repository;

import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.repository.mapper.PlayerRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PlayerRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PlayerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(Player player) {
        jdbcTemplate.update("insert into player (name, role, alive, position, candidate, vote, admin, lobby_id) values (?, ?, ?, ?, ?, ?, ?, ?);",
                player.getName(), player.getRole(), player.getAlive(), player.getPosition(), player.getCandidate(), player.getVote(), player.getAdmin(), player.getLobbyId());
    }

    public List<Player> players() {
        return jdbcTemplate.query("SELECT * FROM player", new PlayerRowMapper());
    }

    public Player selectCurrentPlayerById(int id) {
        return jdbcTemplate.queryForObject("SELECT pl.* FROM player pl WHERE pl.id = ?",
                new PlayerRowMapper(), id);
    }

    public Player selectCurrentPlayerByLobbyId(int lobbyId, String playerName) {
        return jdbcTemplate.queryForObject("SELECT pl.* FROM player pl WHERE pl.lobby_id = ? AND pl.name = ?",
                new PlayerRowMapper(), lobbyId, playerName);
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM player WHERE id=?", id);
    }

    public void deleteByLobbyId(int lobbyId) {
        jdbcTemplate.update("DELETE FROM player WHERE lobby_id=?", lobbyId);
    }

    public void updatePlayer(int position, boolean alive, String playerRole, int playerId) {
        jdbcTemplate.update("UPDATE player SET role = ?, alive = ?, position = ? WHERE id = ?", playerRole, alive, position, playerId);
    }

    public void updateFullPlayer(Player player) {
        jdbcTemplate.update("UPDATE player SET name = ?, role = ?, alive = ?, position = ?, candidate = ?, vote = ?, admin = ?, lobby_id = ? WHERE id = ?",
                player.getName(), player.getRole(), player.getAlive(), player.getPosition(), player.getCandidate(), player.getVote(), player.getAdmin(), player.getLobbyId(), player.getId());
    }
}