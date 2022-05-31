package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.controller.dto.ConnectRequest;
import org.mafiagame.mafia.controller.dto.CreateLobbyRequest;
import org.mafiagame.mafia.exception.InvalidLobbyException;
import org.mafiagame.mafia.exception.InvalidLobbyNumberException;
import org.mafiagame.mafia.exception.InvalidLobbySizeException;
import org.mafiagame.mafia.exception.InvalidPlayerNameException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.service.LobbyService;
import org.mafiagame.mafia.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@Slf4j
@RequestMapping("/v1")
public class LobbyController {
    private final LobbyService lobbyService;
    private final PlayerService playerService;

    @Autowired
    public LobbyController(LobbyService lobbyService, PlayerService playerService) {
        this.lobbyService = lobbyService;
        this.playerService = playerService;
    }

    @PostMapping("/lobby")
    public ResponseEntity<Lobby> addLobby(@RequestBody CreateLobbyRequest createLobbyRequest) throws InvalidLobbyNumberException, InvalidPlayerNameException {
        log.info("Lobby created: {}", createLobbyRequest);
        return ResponseEntity.ok(lobbyService.createGameLobby(createLobbyRequest));
    }

    @GetMapping("/lobby")
    public ResponseEntity<Lobby> getLobbyByNumber(@RequestParam("number") Integer number) throws InvalidLobbyException {
        log.info("Get lobby by number: {}", number);
        return ResponseEntity.ok(lobbyService.getLobbyByNumber(number));
    }

    @PostMapping("/lobby/{number}/players")
    public ResponseEntity<Player> connectUserToLobby(@RequestBody ConnectRequest connectRequest, @PathVariable Integer number) throws InvalidLobbyException, InvalidPlayerNameException, InvalidLobbySizeException {
        log.info("Connect player to lobby: connect req {}, number {}", connectRequest, number);
        return ResponseEntity.ok(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
    }

    @GetMapping("/lobby/{number}/players")
    public ResponseEntity<List<Player>> getPlayersInLobby(@PathVariable Integer number) {
        log.info("Get players in lobby: {}", number);
        return ResponseEntity.ok(lobbyService.getPlayersInLobby(number));
    }

    @DeleteMapping("/lobby/{player_id}")
    public ResponseEntity<String> removePlayerByAdmin(@PathVariable Integer player_id) {
        playerService.deletePlayer(player_id);
        log.info("Delete players in lobby: {}", player_id);
        return ResponseEntity.ok("Player deleted!");
    }

    @GetMapping("/lobbies")
    public ResponseEntity<List<Lobby>> getLobbies() {
        log.info("Lobbies: {}", lobbyService.getLobbies());
        return ResponseEntity.ok(lobbyService.getLobbies());
    }

    @DeleteMapping("/lobby")
    public ResponseEntity<String> deleteLobby(@RequestParam("id") Integer id) {
        log.info("Lobby deleted by id: {}", id);
        lobbyService.deleteLobby(id);
        return ResponseEntity.noContent().build();
    }
}
