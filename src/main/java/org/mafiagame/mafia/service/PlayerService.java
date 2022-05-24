package org.mafiagame.mafia.service;

import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.repository.LobbyRepository;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.mafiagame.mafia.storage.LobbyStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final LobbyRepository lobbyRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository, LobbyRepository lobbyRepository) {
        this.playerRepository = playerRepository;
        this.lobbyRepository = lobbyRepository;
    }

    public void addPlayer(Player player) {
         playerRepository.add(player);
    }

    public List<Player> getPlayers() {
         return playerRepository.players();
    }

    public void deletePlayer(Integer id) {
        Player currPlayer = playerRepository.selectCurrentPlayerById(id);
        Lobby currLobby = lobbyRepository.selectCurrentLobbyByPlayerLobbyId(currPlayer.getLobbyId());
        List<Player> players = LobbyStorage.getInstance().getPlayers(currLobby.getNumber());
        players.remove(currPlayer);
        currLobby.setPlayersList(players);
        LobbyStorage.getInstance().setPlayersAndLobby(currLobby.getNumber(), currLobby);
        playerRepository.delete(id);
    }
}
