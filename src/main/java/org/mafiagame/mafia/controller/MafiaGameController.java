package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.controller.dto.CandidateRequest;
import org.mafiagame.mafia.exception.InvalidGameException;
import org.mafiagame.mafia.exception.InvalidLobbySizeException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.game.MafiaGame;
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
    private final SimpMessagingTemplate webSocket;

    @Autowired
    public MafiaGameController(LobbyService lobbyService, SimpMessagingTemplate webSocket) {
        this.lobbyService = lobbyService;
        this.webSocket = webSocket;
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

    @PostMapping("/lobby/{number}/speech")
    public ResponseEntity<MafiaGame> gameSpeech(@PathVariable Integer number) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = lobbyService.speech(number);
        log.info("Candidate is speaking by number: {}", number);
        return ResponseEntity.ok(mafiaGame);
    }

    @PostMapping("/lobby/{number}/abstain")
    public ResponseEntity<MafiaGame> abstain(@PathVariable Integer number) {
        lobbyService.abstainVote(number);
        log.info("Candidate abstained his vote by number: {}", number);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/lobby/{number}/skip")
    public ResponseEntity<MafiaGame> skip(@PathVariable Integer number) {
        lobbyService.skipSpeech(number);
        log.info("Candidate skipped his speech in game by number: {}", number);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/lobby/{number}/candidates")
    public ResponseEntity<MafiaGame> candidateNomination(@PathVariable Integer number,
                                                         @RequestParam(value = "player_position", required = false) Integer player_position) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = lobbyService./*civilianVoting*/dayVoting(number, player_position);
        webSocket.convertAndSend("/topic/game-progress/" + number, mafiaGame);
        log.info("Candidate nominated by number: {}", number);
        return ResponseEntity.ok(mafiaGame);
    }

    @PostMapping("/lobby/{number}/mafia_turn")
    public ResponseEntity<MafiaGame> mafiaNomination(@PathVariable Integer number,
                                                     @RequestParam(value = "player_position", required = false) Integer player_position) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = lobbyService./*mafiaTurn*/nightMafiaVoting(number, player_position);
        webSocket.convertAndSend("/topic/game-progress/" + number, mafiaGame);
        log.info("Mafia nominated by number: {}", number);
        return ResponseEntity.ok(mafiaGame);
    }

    @PostMapping("/lobby/{number}/sheriff_turn")
    public ResponseEntity<MafiaGame> sheriffCheck(@PathVariable Integer number,
                                                  @RequestParam(value = "player_position", required = false) Integer player_position) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = lobbyService./*sheriffTurn*/nightSheriffVoting(number, player_position);
        webSocket.convertAndSend("/topic/game-progress/" + number, mafiaGame);
        log.info("Sheriff checked by number: {}", number);
        return ResponseEntity.ok(mafiaGame);
    }
}
