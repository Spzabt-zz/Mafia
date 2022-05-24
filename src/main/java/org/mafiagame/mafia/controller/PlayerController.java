package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1")
public class PlayerController {
    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/player")
    public ResponseEntity<String> addPlayer(@RequestBody Player player) {
        log.info("Add player: {}", player);
        playerService.addPlayer(player);
        return ResponseEntity.ok("Player added");
    }

    @GetMapping("/players")
    public ResponseEntity<List<Player>> getPlayers() {
        log.info("Get players");
        return ResponseEntity.ok(playerService.getPlayers());
    }
}
