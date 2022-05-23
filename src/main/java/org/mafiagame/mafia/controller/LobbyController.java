package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.controller.dto.ConnectRequest;
import org.mafiagame.mafia.exception.InvalidLobbyException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@Slf4j
@RequestMapping("/v1")
public class LobbyController {
    private final LobbyService lobbyService;
    private SimpMessagingTemplate simpleMessageTemplate;

    @Autowired
    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    //todo: request lobby name
    @PostMapping("/lobby")
    public ResponseEntity<Lobby> addLobby(@RequestBody Player admin) {
        try {
            return ResponseEntity.ok(lobbyService.createGameLobby(admin));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.createGameLobby(admin));
        }
    }

    //todo: Пошук ігрового лоббі
    @GetMapping("/lobby/{number}:={number}")
    public ResponseEntity findLobby(@PathVariable Integer number) {
        try {
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("error");
        }
    }

    //todo: Повернути 1 player'а
    @PostMapping("/lobby/{number}/players")
    public ResponseEntity<Lobby> connectUserToLobby(@RequestBody ConnectRequest connectRequest, @PathVariable Integer number) throws InvalidLobbyException {
        try {
            return ResponseEntity.ok(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        }
    }

    //todo: Гравці в лоббі
    /*@GetMapping("/lobby/{number}/players")
    public ResponseEntity<Lobby> getLobbyPlayers(@RequestBody ConnectRequest connectRequest, @PathVariable Integer number) throws InvalidLobbyException {
        try {
            return ResponseEntity.ok(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        }
    }*/

    //todo: Адмін може видалити гравця з лоббі
    /*@DeleteMapping("/lobby/{player_id}")
    public ResponseEntity<Lobby> deletePlayerFromLobby(@RequestBody ConnectRequest connectRequest, @PathVariable Integer number) throws InvalidLobbyException {
        try {
            return ResponseEntity.ok(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.connectUserToLobby(connectRequest.getPlayerName(), number));
        }
    }*/

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

    @DeleteMapping("/lobby/{id}")
    public ResponseEntity deleteLobby(@PathVariable Integer id) {
        try {
            lobbyService.deleteLobby(id);
            return ok("Lobby deleted!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }
}
