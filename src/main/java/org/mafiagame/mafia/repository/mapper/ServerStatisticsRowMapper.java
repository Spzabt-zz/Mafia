package org.mafiagame.mafia.repository.mapper;

import org.mafiagame.mafia.model.ServerStatistics;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerStatisticsRowMapper implements RowMapper<ServerStatistics> {
    @Override
    public ServerStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
        ServerStatistics serverStatistics = new ServerStatistics();
        serverStatistics.setMafiaWinCount(rs.getInt("mafia_win_count"));
        serverStatistics.setFairWinCount(rs.getInt("fair_win_count"));
        serverStatistics.setTotalGameCount(rs.getInt("total_game_count"));

        return serverStatistics;
    }
}
