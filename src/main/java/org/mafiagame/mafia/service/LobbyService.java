package org.mafiagame.mafia.service;

import org.mafiagame.mafia.controller.dto.CreateLobbyRequest;
import org.mafiagame.mafia.exception.InvalidLobbyException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.repository.LobbyRepository;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.mafiagame.mafia.storage.GameStorage;
import org.mafiagame.mafia.storage.LobbyStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
public class LobbyService {
    private final int MAX_COUNT_PLAYERS_IN_LOBBY = 12;
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository, PlayerRepository playerRepository) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
    }

    public Lobby createGameLobby(CreateLobbyRequest lobbyRequest) {
        Lobby lobby = new Lobby();
        lobby.setName(lobbyRequest.getLobbyName());
        lobby.setNumber(getRandomNumberUsingNextInt(100000, 999999));
        lobby.setGameStatus(true);
        addLobby(lobby);

        lobby = lobbyRepository.selectCurrentLobbyByNumber(lobby.getNumber());

        Player admin = new Player();

        admin.setName(lobbyRequest.getAdminName());
        /*admin.setRole("mafia");
        admin.setAlive(true);
        admin.setPosition(1);
        admin.setCandidate(false);
        admin.setVote(0);*/
        admin.setAdmin(true);
        admin.setLobbyId(lobby.getId());

        playerRepository.add(admin);

        lobby.setPlayers(admin);
        LobbyStorage.getInstance().setPlayers(lobby.getNumber(), lobby);
        LobbyStorage.getInstance().setLobby(lobby);
        return lobby;
    }

    public Lobby getLobbyByNumber(Integer number) {
        return lobbyRepository.selectCurrentLobbyByNumber(number);
    }

    public Player connectUserToLobby(String playerName, Integer number) throws InvalidLobbyException {
        if (!LobbyStorage.getInstance().getLobby().containsKey(number)) {
            throw new InvalidLobbyException("Game by number: " + number + " doesn't exist");
        }
        Lobby lobby = LobbyStorage.getInstance().getLobby().get(number);

        if (lobby.getPlayers().size() > MAX_COUNT_PLAYERS_IN_LOBBY - 1) {
            throw new InvalidLobbyException("Max players in lobby");
        }

        Player player = new Player();
        player.setName(playerName);
        player.setLobbyId(lobby.getId());
        lobby.setPlayers(player);
        LobbyStorage.getInstance().setPlayers(lobby.getNumber(), lobby);
        LobbyStorage.getInstance().setLobby(lobby);
        playerRepository.add(player);

        return player;
    }
    /*public Lobby connectUserToLobby(String playerName, Integer number) throws InvalidLobbyException {
        if (!LobbyStorage.getInstance().getLobby().containsKey(number)) {
            throw new InvalidLobbyException("Game by number: " + number + " doesn't exist");
        }
        Lobby lobby = LobbyStorage.getInstance().getLobby().get(number);

        Player player = new Player();
        player.setName(playerName);
        player.setLobbyId(lobby.getId());
        lobby.setPlayers(player);
        LobbyStorage.getInstance().setPlayers(lobby.getNumber(), lobby);
        LobbyStorage.getInstance().setLobby(lobby);
        playerRepository.add(player);

        return lobby;
    }*/

    public int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public void addLobby(Lobby lobby) {
        lobbyRepository.add(lobby);
    }

    public List<Lobby> getLobbies() {
        return lobbyRepository.lobbies();
    }

    public void deleteLobby(Integer id) {
        lobbyRepository.delete(id);
    }
}
