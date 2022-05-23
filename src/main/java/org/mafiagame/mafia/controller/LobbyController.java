package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.controller.dto.ConnectRequest;
import org.mafiagame.mafia.controller.dto.CreateLobbyRequest;
import org.mafiagame.mafia.exception.InvalidLobbyException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.service.LobbyService;
import org.mafiagame.mafia.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@Slf4j
@RequestMapping("/v1")
public class LobbyController {
    private final LobbyService lobbyService;
    private final PlayerService playerService;
    private SimpMessagingTemplate simpleMessageTemplate;

    @Autowired
    public LobbyController(LobbyService lobbyService, PlayerService playerService) {
        this.lobbyService = lobbyService;
        this.playerService = playerService;
    }

    //todo: request lobby name - OK
    @PostMapping("/lobby")
    public ResponseEntity<Lobby> addLobby(@RequestBody CreateLobbyRequest createLobbyRequest) {
        try {
            return ResponseEntity.ok(lobbyService.createGameLobby(createLobbyRequest));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.createGameLobby(createLobbyRequest));
        }
    }

    //todo: Пошук ігрового лоббі - DONE
    @GetMapping("/lobby")
    public ResponseEntity<Lobby> getLobbyByNumber(@RequestParam("number") @PathVariable Integer number) {
        try {
            return ResponseEntity.ok(lobbyService.getLobbyByNumber(number));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.getLobbyByNumber(number));
        }
    }

    //todo: Повернути 1 player'а - DONE
    @PostMapping("/lobby/{number}/players")
    public ResponseEntity<Player> connectUserToLobby(@RequestBody ConnectRequest connectRequest, @PathVariable Integer number) throws InvalidLobbyException {
        try {
            return ResponseEntity.ok(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        }
    }
    /*@PostMapping("/lobby/{number}/players")
    public ResponseEntity<Lobby> connectUserToLobby(@RequestBody ConnectRequest connectRequest, @PathVariable Integer number) throws InvalidLobbyException {
        try {
            return ResponseEntity.ok(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        }
    }*/

    //todo: Гравці в лоббі - DONE
    @GetMapping("/lobby/{number}/players")
    public ResponseEntity<List<Player>> getPlayersByLobby(@PathVariable Integer number) {
        try {
            List<Player> playersByLobby = playerService.getPlayers()
                    .stream()
                    .filter(player -> Objects.equals(player.getLobbyId(), lobbyService.getLobbyByNumber(number).getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(playersByLobby);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(playerService.getPlayers());
        }
    }

    //todo: Адмін може видалити гравця з лоббі
    @DeleteMapping("/lobby/{player_id}")
    public ResponseEntity<String> removePlayerByAdmin(@PathVariable Integer player_id) {
        try {
            playerService.deletePlayer(player_id);
            return ok("Player deleted!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }

    @GetMapping("/lobbies")
    public ResponseEntity<List<Lobby>> getLobbies() {
        try {
            log.info("Lobbies: {}", lobbyService.getLobbies());
            return ResponseEntity.ok(lobbyService.getLobbies());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.getLobbies());
        }
    }

    /*@DeleteMapping("/lobby/{id}")
    public ResponseEntity deleteLobby(@PathVariable Integer id) {
        try {
            lobbyService.deleteLobby(id);
            return ok("Lobby deleted!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }*/
}
