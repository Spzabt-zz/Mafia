package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.controller.dto.CandidateRequest;
import org.mafiagame.mafia.exception.InvalidLobbySizeException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.game.MafiaGame;
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
    private final LobbyService lobbyService;
    private SimpMessagingTemplate simpleMessageTemplate;

    @Autowired
    public MafiaGameController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PutMapping("/lobby/{number}/mafia_game")
    public ResponseEntity<Lobby> startGame(@PathVariable Integer number) throws InvalidLobbySizeException {
        log.info("Game started at lobby number: {}", number);
        return ResponseEntity.ok(lobbyService.startGame(number));
    }
    @GetMapping("/lobby/{number}/mafia_game")
    public ResponseEntity<MafiaGame> gameStatus(@PathVariable Integer number) {
        log.info("Game status at lobby number: {}", number);
        return ResponseEntity.ok(lobbyService.getGameStatus(number));
    }

    @PostMapping("/lobby/{number}/candidates")
    public ResponseEntity candidateNomination(@PathVariable Integer number, @RequestBody CandidateRequest request) {

        //simpleMessageTemplate.convertAndSend("/topic/game-progress/" + number, request);
        log.info("Candidate nominated by number: {}", number);
        return ResponseEntity.ok("OK");
    }
}
