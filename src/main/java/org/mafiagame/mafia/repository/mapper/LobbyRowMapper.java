package org.mafiagame.mafia.repository.mapper;

import org.mafiagame.mafia.model.GameStatus;
import org.mafiagame.mafia.model.Lobby;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LobbyRowMapper implements RowMapper<Lobby> {
    @Override
    public Lobby mapRow(ResultSet rs, int rowNum) throws SQLException {
        Lobby lobby = new Lobby();
        lobby.setId(rs.getInt("id"));
        lobby.setName(rs.getString("name"));
        lobby.setNumber(rs.getInt("number"));
        lobby.setGameStatus(rs.getString("game_status"));

        return lobby;
    }
}
