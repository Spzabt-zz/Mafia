package org.mafiagame.mafia.controller;

import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class PlayerController {
    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/player")
    public ResponseEntity addPlayer(@RequestBody Player player) {
        try {
            playerService.addPlayer(player);
            return ResponseEntity.ok("Player added");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Some error - " + e.getMessage());
        }
    }

    @GetMapping("/players")
    public ResponseEntity getPlayers() {
        try {
            return ResponseEntity.ok("Everything is working! " + playerService.getPlayers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity deletePlayer(@PathVariable Integer id) {
        try {
            playerService.deletePlayer(id);
            return ResponseEntity.ok("Player deleted!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }
}
