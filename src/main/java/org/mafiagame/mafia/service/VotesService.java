package org.mafiagame.mafia.service;

import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.repository.VotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VotesService {
    private final VotesRepository votesRepository;

    @Autowired
    public VotesService(VotesRepository votesRepository) {
        this.votesRepository = votesRepository;
    }

    public void addVote(Votes votes) {
        votesRepository.add(votes);
    }

    public List<Votes> getVotes() {
        return votesRepository.votes();
    }

    public void deleteVote(Integer id) {
        votesRepository.delete(id);
    }
}
