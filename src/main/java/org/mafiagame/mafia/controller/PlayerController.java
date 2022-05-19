package org.mafiagame.mafia.controller;

import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
public class PlayerController {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerController(PlayerRepository userRepository) {
        this.playerRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity addPlayer(@RequestBody Player player) {
        try {
            playerRepository.addPlayer(player);
            return ResponseEntity.ok("User are added!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity getPlayers() {
        try {
            return ResponseEntity.ok("Everything is working!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error!");
        }
    }
}
