package org.mafiagame.mafia.service;

import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.repository.LobbyRepository;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LobbyService {
    private final LobbyRepository lobbyRepository;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    public void addLobby(Lobby lobby) {
        lobbyRepository.add(lobby);
    }

    /*public List<Lobby> getLobby() {
        return playerRepository.players();
    }

    public void deleteLobby(Lobby lobby) {
        playerRepository.delete(player.getId());
    }*/
}
