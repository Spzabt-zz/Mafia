package org.mafiagame.mafia.repository.mapper;

import org.mafiagame.mafia.model.Player;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerRowMapper implements RowMapper<Player> {

    @Override
    public Player mapRow(ResultSet rs, int rowNum) throws SQLException {
        Player player = new Player();
        player.setId(rs.getInt("id"));
        player.setName(rs.getString("name"));
        player.setRole(rs.getString("role"));
        player.setAlive(rs.getBoolean("alive"));
        player.setPosition(rs.getInt("position"));
        player.setCandidate(rs.getBoolean("candidate"));
        player.setVote(rs.getInt("vote"));
        player.setAdmin(rs.getBoolean("admin"));
        player.setLobbyId(rs.getInt("lobby_id"));

        return player;
    }
}
