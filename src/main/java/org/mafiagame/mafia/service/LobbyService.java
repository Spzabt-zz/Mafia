package org.mafiagame.mafia.service;

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
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;
    //private Lobby lobby;
    //private List<Player> players;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository, PlayerRepository playerRepository) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
    }

    public Lobby createGameLobby(/*List<Player> players,*/ Player admin) {
        Lobby lobby = new Lobby();
        lobby.setNumber(getRandomNumberUsingNextInt(100000, 999999));
        lobby.setGameStatus(true);
        addLobby(lobby);

        lobby = lobbyRepository.selectCurrentLobbyByNumber(lobby.getNumber());

        admin.setName("admin");
        admin.setRole("mafia");
        admin.setAlive(true);
        admin.setPosition(1);
        admin.setCandidate(false);
        admin.setVote(0);
        admin.setAdmin(true);
        admin.setLobbyId(lobby.getId());

        playerRepository.add(admin);

        //List<Player> players = new ArrayList<>();
        //players.add(admin);

        //lobby.setPlayers(players);
        lobby.setPlayers(admin);
        LobbyStorage.getInstance().setPlayers(lobby.getNumber(), lobby);
        //lobby.setPlayers(LobbyStorage.getInstance().getPlayers());
        LobbyStorage.getInstance().setLobby(lobby);
        return lobby;
    }

    public Lobby connectUserToLobby(Player player, Integer number) throws InvalidLobbyException {
        if (!LobbyStorage.getInstance().getLobby().containsKey(number)) {
            throw new InvalidLobbyException("Game by number: " + number + " doesn't exist");
        }
        Lobby lobby = LobbyStorage.getInstance().getLobby().get(number);

        player.setLobbyId(lobby.getId());
        lobby.setPlayers(player);
        LobbyStorage.getInstance().setPlayers(lobby.getNumber(), lobby);
        //lobby.setPlayers(LobbyStorage.getInstance().getPlayers());
        LobbyStorage.getInstance().setLobby(lobby);
        playerRepository.add(player);

        /*Lobby lobby = lobbyRepository.selectCurrentLobbyByNumber(number);
        player.setLobbyId(lobby.getId());
        List<Player> players = new ArrayList<>();
        players.add(player);
        lobby.setPlayers(players);*/

        return lobby;
    }

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
