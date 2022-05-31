package org.mafiagame.mafia.service;

import org.mafiagame.mafia.controller.dto.CreateLobbyRequest;
import org.mafiagame.mafia.exception.InvalidLobbyException;
import org.mafiagame.mafia.exception.InvalidLobbyNumberException;
import org.mafiagame.mafia.exception.InvalidLobbySizeException;
import org.mafiagame.mafia.exception.InvalidPlayerNameException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.model.enam.DayTime;
import org.mafiagame.mafia.model.enam.GameStatus;
import org.mafiagame.mafia.model.enam.Phase;
import org.mafiagame.mafia.model.enam.PlayerRole;
import org.mafiagame.mafia.model.game.MafiaGame;
import org.mafiagame.mafia.repository.LobbyRepository;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.mafiagame.mafia.repository.VotesRepository;
import org.mafiagame.mafia.storage.GameStorage;
import org.mafiagame.mafia.storage.LobbyStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class LobbyService {
    private static final int MIN_COUNT_PLAYERS_IN_LOBBY = 4;
    private static final int MAX_COUNT_PLAYERS_IN_LOBBY = 12;
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;
    private final VotesRepository votesRepository;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository, PlayerRepository playerRepository, VotesRepository votesRepository) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
        this.votesRepository = votesRepository;
    }

    public Lobby createGameLobby(CreateLobbyRequest lobbyRequest) throws InvalidLobbyNumberException, InvalidPlayerNameException {
        if (lobbyRequest.getAdminName() == null) {
            throw new InvalidPlayerNameException("Name can't be null");
        }

        Lobby lobby = new Lobby();
        lobby.setName(lobbyRequest.getLobbyName());

        int lbNumber = getRandomNumberUsingNextInt(100000, 999999);
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
            throw  new InvalidPlayerNameException("Name can't be null");
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

    //todo change method
    public Lobby startGame(Integer number) throws InvalidLobbySizeException {
        if (LobbyStorage.getInstance().getLobby().get(number).getPlayers().size() < MIN_COUNT_PLAYERS_IN_LOBBY) {
            throw new InvalidLobbySizeException("Min players in lobby " + MIN_COUNT_PLAYERS_IN_LOBBY + ". Invite more people");
        }
        Lobby currLobby = LobbyStorage.getInstance().getLobby().get(number);
        currLobby.setGameStatus(GameStatus.IN_PROGRESS.toString());
        Lobby modifiedLobby = setPlayerStats(currLobby);
        LobbyStorage.getInstance().setLobby(modifiedLobby);
        lobbyRepository.updateLobbyStatus(number);

        MafiaGame mafiaGame = new MafiaGame();
        mafiaGame.setDay(1);
        mafiaGame.setDayTime(DayTime.NIGHT);
        mafiaGame.setPhase(Phase.SPEECH);
        mafiaGame.setLobby(currLobby);
        GameStorage.getInstance().setGame(mafiaGame);

        return currLobby;
    }

    private Lobby setPlayerStats(Lobby lobby) {
        int count = 0;
        int playerCount = lobby.getPlayers().size();
        List<String> listOfPlayerRoles = null;

        /*int s = 1;
        int x = (playerCount * 2 - 3) / 3;*/

        int sheriffCount = 1;
        switch (playerCount) {
            case 4:
            case 5:
                listOfPlayerRoles = new ArrayList<>(List.of(PlayerRole.MAFIA.toString(), PlayerRole.SHERIFF.toString()));
                int mafiaCount = 1;
                int civilianCount = playerCount - sheriffCount - mafiaCount;
                for (int i = 0; i < civilianCount; i++) {
                    listOfPlayerRoles.add(PlayerRole.CIVILIAN.toString());
                }
                break;
            case 6:
            case 7:
            case 8:
                listOfPlayerRoles = new ArrayList<>(List.of(PlayerRole.MAFIA.toString(), PlayerRole.MAFIA.toString(), PlayerRole.SHERIFF.toString()));
                mafiaCount = 2;
                civilianCount = playerCount - sheriffCount - mafiaCount;
                for (int i = 0; i < civilianCount; i++) {
                    listOfPlayerRoles.add(PlayerRole.CIVILIAN.toString());
                }
                break;
            case 9:
            case 10:
            case 11:
            case 12:
                listOfPlayerRoles = new ArrayList<>(List.of(PlayerRole.MAFIA.toString(), PlayerRole.MAFIA.toString(), PlayerRole.MAFIA.toString(), PlayerRole.SHERIFF.toString()));
                mafiaCount = 3;
                civilianCount = playerCount - sheriffCount - mafiaCount;
                for (int i = 0; i < civilianCount; i++) {
                    listOfPlayerRoles.add(PlayerRole.CIVILIAN.toString());
                }
                break;
            default:
                break;
        }

        for (Player player : lobby.getPlayers()) {
            count++;
            assert listOfPlayerRoles != null;
            player.setRole(getRandomRole(listOfPlayerRoles));
            player.setAlive(true);
            player.setPosition(count);
            Votes currVote = new Votes(player.getId(), 0, 1);
            player.setVotes(currVote);
            playerRepository.updatePlayer(player.getPosition(), player.getAlive(), player.getRole(), player.getId());
            votesRepository.add(currVote);
        }

        return lobby;
    }

    private String getRandomRole(List<String> playerRoles) {
        int randomIndex = 0;
        if (playerRoles.size() != 1) {
            randomIndex = getRandomNumberUsingNextInt(0, playerRoles.size());
        }
        String randomElement = playerRoles.get(randomIndex);
        playerRoles.remove(randomIndex);

        return randomElement;
    }

    public MafiaGame getGameStatus(Integer number) {
        return GameStorage.getInstance().getMafiaGame(number);
    }

    //public

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
