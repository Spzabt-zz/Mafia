package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.service.VotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1")
public class VotesController {
    private final VotesService votesService;

    @Autowired
    public VotesController(VotesService votesService) {
        this.votesService = votesService;
    }

    @PostMapping("/vote")
    public ResponseEntity<String> addVote(@RequestBody Votes votes) {
        log.info("Add vote: {}", votes);
        votesService.addVote(votes);
        return ResponseEntity.ok("Vote added");
    }

    @GetMapping("/votes")
    public ResponseEntity<List<Votes>> getVotes() {
        log.info("Get votes");
        return ResponseEntity.ok(votesService.getVotes());
    }

    @DeleteMapping("/votes/{id}")
    public ResponseEntity<String> deleteVote(@PathVariable Integer id) {
        log.info("Delete vote: {}", id);
        votesService.deleteVote(id);
        return ResponseEntity.ok("Vote deleted!");
    }
}
