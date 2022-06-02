package org.mafiagame.mafia.repository;

import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.repository.mapper.VotesRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VotesRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public VotesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(Votes votes) {
        jdbcTemplate.update("insert into votes (player_id ,candidate_id, day) values (?, ?, ?)",
                votes.getPlayerId(), votes.getCandidateId(), votes.getDay());
    }

    public List<Votes> votes() {
        return jdbcTemplate.query("SELECT * FROM votes", new VotesRowMapper());
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM votes WHERE player_id=?", id);
    }

    public void updatePlayerVotes(Votes votes) {
        jdbcTemplate.update("UPDATE votes SET candidate_id = ?, day = ? WHERE player_id = ?", votes.getCandidateId(), votes.getDay(), votes.getPlayerId());
    }
}
