package org.mafiagame.mafia.service;

import org.mafiagame.mafia.controller.dto.CreateLobbyRequest;
import org.mafiagame.mafia.exception.InvalidLobbyException;
import org.mafiagame.mafia.exception.InvalidLobbyNumberException;
import org.mafiagame.mafia.exception.InvalidLobbySizeException;
import org.mafiagame.mafia.exception.InvalidPlayerNameException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.model.enam.GameStatus;
import org.mafiagame.mafia.model.enam.PlayerRole;
import org.mafiagame.mafia.repository.LobbyRepository;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.mafiagame.mafia.repository.VotesRepository;
import org.mafiagame.mafia.service.logic.GameLogic;
import org.mafiagame.mafia.storage.LobbyStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LobbyService {
    private static final int MAX_COUNT_PLAYERS_IN_LOBBY = 12;
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;
    private final VotesRepository votesRepository;
    private final GameLogic gameLogic;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository, PlayerRepository playerRepository, VotesRepository votesRepository, GameLogic gameLogic) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
        this.votesRepository = votesRepository;
        this.gameLogic = gameLogic;
    }

    public Lobby createGameLobby(CreateLobbyRequest lobbyRequest) throws InvalidLobbyNumberException, InvalidPlayerNameException {
        if (lobbyRequest.getAdminName() == null) {
            throw new InvalidPlayerNameException("Name can't be null");
        }

        Lobby lobby = new Lobby();
        lobby.setName(lobbyRequest.getLobbyName());

        int lbNumber = gameLogic.getRandomNumberUsingNextInt(100000, 999999);
        for (Lobby lobbyNumber : lobbyRepository.lobbies()) {
            if (lobbyNumber.getNumber() == lbNumber) {
                throw new InvalidLobbyNumberException("Lobby with number: " + lbNumber + " already exists");
            }
        }

        lobby.setNumber(lbNumber);
        lobby.setGameStatus(GameStatus.NEW.toString());
        addLobby(lobby);

        lobby = lobbyRepository.selectCurrentLobbyByNumber(lobby.getNumber());

        Player admin = new Player();

        admin.setName(lobbyRequest.getAdminName());
        admin.setRole(PlayerRole.DEFAULT.toString());
        admin.setAlive(false);
        admin.setPosition(0);
        admin.setCandidate(false);
        admin.setVote(0);
        admin.setAdmin(true);
        admin.setLobbyId(lobby.getId());

        playerRepository.add(admin);

        Player adminForIdSetting = playerRepository.selectCurrentPlayerByLobbyId(lobby.getId(), lobbyRequest.getAdminName());
        admin.setId(adminForIdSetting.getId());

        lobby.setPlayers(admin);
        LobbyStorage.getInstance().setPlayersAndLobby(lobby.getNumber(), lobby);
        return lobby;
    }

    public Player connectUserToLobby(String playerName, Integer number) throws InvalidLobbyException, InvalidPlayerNameException, InvalidLobbySizeException {
        if (playerName == null) {
            throw new InvalidPlayerNameException("Name can't be null");
        }
        if (!LobbyStorage.getInstance().getLobby().containsKey(number)) {
            throw new InvalidLobbyException("Game by number: " + number + " doesn't exist");
        }

        Lobby lobby = LobbyStorage.getInstance().getLobby().get(number);

        if (lobby.getPlayers().size() > MAX_COUNT_PLAYERS_IN_LOBBY - 1) {
            throw new InvalidLobbySizeException("In lobby already max players");
        }
        for (Player player : lobby.getPlayers()) {
            if (player.getName().equals(playerName)) {
                throw new InvalidPlayerNameException("Player with name " + playerName + " already exists in lobby");
            }
        }
        if (Objects.equals(lobby.getGameStatus(), GameStatus.IN_PROGRESS.toString())) {
            throw new InvalidLobbyException("Players already playing in current lobby by number: " + number);
        }

        Player player = new Player();
        player.setName(playerName);
        player.setRole(PlayerRole.DEFAULT.toString());
        player.setAlive(false);
        player.setPosition(0);
        player.setCandidate(false);
        player.setVote(0);
        player.setAdmin(false);
        player.setLobbyId(lobby.getId());

        playerRepository.add(player);

        Player playerForIdSetting = playerRepository.selectCurrentPlayerByLobbyId(lobby.getId(), playerName);
        player.setId(playerForIdSetting.getId());

        lobby.setPlayers(player);
        LobbyStorage.getInstance().setPlayersAndLobby(lobby.getNumber(), lobby);

        return player;
    }

    public Lobby getLobbyByNumber(Integer number) throws InvalidLobbyException {
        if (!LobbyStorage.getInstance().getLobby().containsKey(number)) {
            throw new InvalidLobbyException("Game by number: " + number + " doesn't exist");
        }
        return LobbyStorage.getInstance().getLobby().get(number);
    }

    public List<Player> getPlayersInLobby(Integer number) {
        return playerRepository.players()
                .stream()
                .filter(player -> {
                    try {
                        return Objects.equals(player.getLobbyId(), getLobbyByNumber(number).getId());
                    } catch (InvalidLobbyException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public void addLobby(Lobby lobby) {
        lobbyRepository.add(lobby);
    }

    public List<Lobby> getLobbies() {
        return lobbyRepository.lobbies();
    }

    public void deleteLobby(Integer id) {
        Lobby currLobby = lobbyRepository.selectCurrentLobbyByPlayerLobbyId(id);
        Map<Integer, Lobby> lobbyMap = LobbyStorage.getInstance().getLobby();
        if (Objects.equals(currLobby.getGameStatus(), GameStatus.NEW.toString())) {
            lobbyMap.remove(currLobby.getNumber());
            LobbyStorage.getInstance().setLobbies(lobbyMap);
            playerRepository.deleteByLobbyId(id);
            lobbyRepository.delete(id);
        } else if (Objects.equals(currLobby.getGameStatus(), GameStatus.FINISHED.toString())) {
            for (Player player : lobbyMap.get(currLobby.getNumber()).getPlayers()) {
                votesRepository.delete(player.getId());
            }
            lobbyMap.remove(currLobby.getNumber());
            LobbyStorage.getInstance().setLobbies(lobbyMap);

            playerRepository.deleteByLobbyId(id);
            lobbyRepository.delete(id);
        }
    }
}