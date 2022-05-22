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

import javax.persistence.Lob;
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

    @PostMapping("/lobby")
    public ResponseEntity<Lobby> addLobby(@RequestBody Player admin) {
        try {
            return ResponseEntity.ok(lobbyService.createGameLobby(admin));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.createGameLobby(admin));
        }
    }

    @PostMapping("/lobby/{number}")
    public ResponseEntity<Lobby> connectUserToLobby(@RequestBody ConnectRequest connectRequest, @PathVariable Integer number) throws InvalidLobbyException {
        try {
            return ResponseEntity.ok(lobbyService.connectUserToLobby(connectRequest.getPlayer(), number));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(lobbyService.connectUserToLobby(connectRequest.getPlayer(), number));
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
