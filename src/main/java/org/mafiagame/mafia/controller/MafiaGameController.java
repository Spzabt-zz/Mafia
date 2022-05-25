package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.exception.InvalidLobbySizeException;
import org.mafiagame.mafia.service.LobbyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/v1")
public class MafiaGameController {
    private final LobbyService lobbyService;

    public MafiaGameController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

//    @PutMapping("/lobby/{number}/mafia_game")
//    public ResponseEntity startGame(@PathVariable Integer number) {
//        return ResponseEntity.ok("Game started");
//    }

    @GetMapping("/lobby/{number}/mafia_game")
    public ResponseEntity gameStatus(@PathVariable Integer number) {
        return ResponseEntity.ok("Game status");
    }

    @PutMapping("/lobby/{number}/mafia_game")
    public ResponseEntity adminStartingGame(@PathVariable Integer number) throws InvalidLobbySizeException {
        return ResponseEntity.ok(lobbyService.startGame(number));
    }
}
