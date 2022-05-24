package org.mafiagame.mafia.service;

import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void addPlayer(Player player) {
         playerRepository.add(player);
    }

    public List<Player> getPlayers() {
         return playerRepository.players();
    }

    public void deletePlayer(Integer id) {
        playerRepository.delete(id);
    }
}
