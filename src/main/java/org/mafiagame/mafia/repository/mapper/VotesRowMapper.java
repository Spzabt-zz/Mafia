package org.mafiagame.mafia.repository.mapper;

import org.mafiagame.mafia.model.Votes;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VotesRowMapper implements RowMapper<Votes> {
    @Override
    public Votes mapRow(ResultSet rs, int rowNum) throws SQLException {
        Votes votes = new Votes();
        votes.setPlayerId(rs.getInt("player_id"));
        votes.setCandidateId(rs.getInt("candidate_id"));
        votes.setDay(rs.getInt("day"));

        return votes;
    }
}
