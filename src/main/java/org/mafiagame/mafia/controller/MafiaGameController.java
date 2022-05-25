package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.exception.InvalidLobbySizeException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.game.MafiaGamePlay;
import org.mafiagame.mafia.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/v1")
public class MafiaGameController {
    private LobbyService lobbyService;
    private SimpMessagingTemplate simpleMessageTemplate;

    @Autowired
    public MafiaGameController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PutMapping("/lobby/{number}/mafia_game")
    public ResponseEntity<Lobby> startGame(@PathVariable Integer number) throws InvalidLobbySizeException {
        return ResponseEntity.ok(lobbyService.startGame(number));
    }

    @GetMapping("/lobby/{number}/mafia_game")
    public ResponseEntity gameStatus(@PathVariable Integer number, @RequestBody MafiaGamePlay request) {
        return ResponseEntity.ok("Game status");
    }
}
