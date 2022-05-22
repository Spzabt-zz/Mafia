package org.mafiagame.mafia.controller;

import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.service.VotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class VotesController {
    private final VotesService votesService;

    @Autowired
    public VotesController(VotesService votesService) {
        this.votesService = votesService;
    }

    @PostMapping("/vote")
    public ResponseEntity addVote(@RequestBody Votes votes) {
        try {
            votesService.addVote(votes);
            return ResponseEntity.ok("Vote added");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Some error - " + e.getMessage());
        }
    }

    @GetMapping("/votes")
    public ResponseEntity getVotes() {
        try {
            return ResponseEntity.ok("Everything is working! " + votesService.getVotes());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }

    @DeleteMapping("/votes/{id}")
    public ResponseEntity deleteVote(@PathVariable Integer id) {
        try {
            votesService.deleteVote(id);
            return ResponseEntity.ok("Vote deleted!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }
}
