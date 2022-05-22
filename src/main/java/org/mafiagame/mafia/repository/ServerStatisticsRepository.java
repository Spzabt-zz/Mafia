package org.mafiagame.mafia.repository;

import org.mafiagame.mafia.model.ServerStatistics;
import org.mafiagame.mafia.repository.mapper.ServerStatisticsRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ServerStatisticsRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ServerStatisticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(ServerStatistics serverStatistics) {
        jdbcTemplate.update("insert into server_statistics (mafia_win_count ,fair_win_count, total_game_count) values (?, ?, ?)",
                serverStatistics.getMafiaWinCount(), serverStatistics.getFairWinCount(), serverStatistics.getTotalGameCount());
    }

    public List<ServerStatistics> serverStatistics() {
        return jdbcTemplate.query("SELECT * FROM server_statistics", new ServerStatisticsRowMapper());
    }
}
