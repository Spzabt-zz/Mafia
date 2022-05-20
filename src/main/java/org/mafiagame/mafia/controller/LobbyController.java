package org.mafiagame.mafia.controller;

import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.service.LobbyService;
import org.mafiagame.mafia.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class LobbyController {
    private final LobbyService lobbyService;

    @Autowired
    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping("/lobby")
    public ResponseEntity addLobby(@RequestBody Lobby lobby) {
        try {
            lobbyService.addLobby(lobby);
            return ResponseEntity.ok("Lobby added");
        } catch (Exception e) {
            //System.out.println(res);
            return ResponseEntity.badRequest().body("Some error - " + e.getMessage());
        }
    }

    @PostMapping("/lobby/{number}/players")
    public ResponseEntity connectUserToLobby(@PathVariable Integer number) {
        try {
            return ResponseEntity.ok("Player connected");
        } catch (Exception e) {
            //System.out.println(res);
            return ResponseEntity.badRequest().body("Some error - " + e.getMessage());
        }
    }

   /* @GetMapping("/get")
    public ResponseEntity getLobbies() {
        try {
            return ResponseEntity.ok("Everything is working!" + lobbyService.getLobbies());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }*/
}
