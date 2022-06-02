package org.mafiagame.mafia.service;

import org.mafiagame.mafia.controller.dto.CreateLobbyRequest;
import org.mafiagame.mafia.exception.*;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.model.enam.*;
import org.mafiagame.mafia.model.game.MafiaGame;
import org.mafiagame.mafia.repository.LobbyRepository;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.mafiagame.mafia.repository.VotesRepository;
import org.mafiagame.mafia.storage.GameStorage;
import org.mafiagame.mafia.storage.LobbyStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

    //todo: change method, implement timer?
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
        mafiaGame.setDay(0);
        mafiaGame.setDayTime(DayTime.NIGHT);
        mafiaGame.setPhase(Phase.MAFIA);
        mafiaGame.setCurrentPlayer(1);
        mafiaGame.setPlayers(modifiedLobby.getPlayers());
        //mafiaGame.setTimer();
        //mafiaGame.setLobby(currLobby);
        GameStorage.getInstance().setGame(mafiaGame, modifiedLobby.getNumber());

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

    //todo: implement timer?
    public MafiaGame civilianVoting(Integer number, Integer candidateId) throws InvalidGameException {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);
        List<Player> players = mafiaGame.getPlayers();

        if (candidateId < 1 || candidateId > players.size()) {
            throw new InvalidGameException("Candidate not found");
        }
        for (Player player : players) {
            if (Objects.equals(player.getPosition(), candidateId) && !player.getAlive()) {
                throw new InvalidGameException("Player: " + player.getName() + " at position " + candidateId + " already killed");
            }
        }
        if (Objects.equals(mafiaGame.getCurrentPlayer(), candidateId)) {
            throw new InvalidGameException("Player can't vote for himself");
        }

        Player player = players.get(mafiaGame.getCurrentPlayer() - 1);

        /*if (!player.getAlive()) {
            player = players.get(mafiaGame.getCurrentPlayer());
        }*/
        int counter = 0;
        while (!player.getAlive()) {
            if (player.getPosition() != players.size()) {
                player = players.get(mafiaGame.getCurrentPlayer() + counter);
                counter++;
            } else break;
        }

        if (mafiaGame.getPhase() != Phase.VOTING) {
            mafiaGame.setDayTime(DayTime.DAY);
            mafiaGame.setPhase(Phase.VOTING);
        }

        Votes votes = player.getVotes();
        votes.setCandidateId(candidateId);
        player.setVotes(votes);
        votesRepository.updatePlayerVotes(votes);

        Player votedPlayer = players.get(candidateId - 1);
        int voteCount = votedPlayer.getVote();
        voteCount++;
        votedPlayer.setVote(voteCount);
        playerRepository.updateFullPlayer(votedPlayer);

        /*Player previousPlayer = players.get(mafiaGame.getCurrentPlayer() - 1);
        if (!previousPlayer.getAlive()) {
            if (mafiaGame.getCurrentPlayer() == players.size() - 1) {
                killPlayerAndCheckWinner(mafiaGame, players, false);
                *//*mafiaGame.setCurrentPlayer(0);
                int playerIndexWithBiggestVoteScore = 0;
                int maxPlayerVoteCount = players.get(0).getVote();

                //todo: handle when vote scores are equals
                for (int i = 1; i < players.size(); i++) {
                    if (players.get(i).getVote() > maxPlayerVoteCount) {
                        playerIndexWithBiggestVoteScore = players.get(i).getVote();
                    }
                }

                Player killPlayer = players.get(playerIndexWithBiggestVoteScore);
                killPlayer.setAlive(false);

                if (checkWinner(players) == WinStatus.FAIR_WIN) {
                    mafiaGame.setWinStatus(WinStatus.FAIR_WIN);
                } else if (checkWinner(players) == WinStatus.MAFIA_WIN) {
                    mafiaGame.setWinStatus(WinStatus.MAFIA_WIN);
                }

                playerRepository.updateFullPlayer(killPlayer);*//*
            }*/

        int votee = 0;
        int cout = 0;
        for (Player player1 : players) {
            votee += player1.getVote();
            if (player1.getAlive()) {
                cout++;
            }
        }

        if (votee == cout) {
            killPlayerAndCheckWinner(mafiaGame, players, true);
        }

      /*  Player nextPlayer = players.get(mafiaGame.getCurrentPlayer());

        int nextCounter = 0;*/

        int currentPlayer = 0;
        int playerPos = 0;
        if (/*player.getPosition() == players.size()*/votee == cout) {
            for (Player player1 : players) {
                if (player1.getAlive()) {
                    playerPos = player1.getPosition();
                    break;
                }
            }
            mafiaGame.setCurrentPlayer(playerPos);
            //currentPlayer = playerPos;
        } else {
            int deadCounter = 1;
            Player nextPlayer = players.get(mafiaGame.getCurrentPlayer());
            if (!nextPlayer.getAlive())
                for (int i = player.getPosition(); i < players.size(); i++) {
                    if (!players.get(i).getAlive()) {
                        if (players.get(i).getAlive())
                            break;
                        deadCounter++;
                    }
                }
            /*currentPlayer = player.getPosition() + deadCounter;*/
            if (player.getPosition() + deadCounter <= players.size())
                if (players.get(player.getPosition() + deadCounter - 1).getAlive())
                    currentPlayer = player.getPosition() + deadCounter;
                else {
                    for (Player player1 : players) {
                        if (player1.getAlive()) {
                            currentPlayer = player1.getPosition();
                            break;
                        }
                    }
                }
            mafiaGame.setCurrentPlayer(currentPlayer);
        }

       /* int counter1 = 0;
        if (player.getPosition() == players.size()) {
            for (Player player1 : players) {
                if (player1.getAlive()) {
                    mafiaGame.setCurrentPlayer(player1.getPosition());
                    break;
                }
            }
        } else {
            if (!players.get(player.getPosition()).getAlive() && (player.getPosition() + 1) <= players.size()) {
                for (int i = player.getPosition(); i < players.size(); i++) {
                    if (!players.get(i).getAlive()) {
                        counter1++;
                    }
                }
                if ((player.getPosition() + counter1) < players.size()) {
                    mafiaGame.setCurrentPlayer(player.getPosition() + counter1);
                } else {
                    for (Player player1 : players) {
                        if (player1.getAlive()) {
                            mafiaGame.setCurrentPlayer(player1.getPosition());
                            break;
                        }
                    }
                }
            }
        }*/

        /*for (int i = player.getPosition() - 1; i < players.size(); i++) {
            if (player.getPosition() == players.size()) {
                mafiaGame.setCurrentPlayer(1);
            }
            if (players.get(i).getAlive()) {
                mafiaGame.setCurrentPlayer(players.get(i).getPosition() + 1);
                break;
            }
        }*/

        /*while (!player.getAlive()) {
            if (player.getPosition() != players.size()) {
                player = players.get(mafiaGame.getCurrentPlayer());

                int currentPlayer;
                currentPlayer = player.getPosition();
                mafiaGame.setCurrentPlayer(currentPlayer);

            } else {
                int currentPlayer = mafiaGame.getCurrentPlayer();
                currentPlayer++;
                mafiaGame.setCurrentPlayer(currentPlayer);
                break;
            }
        }*/

       /* if (!nextPlayer.getAlive()) {
            //while (!nextPlayer.getAlive()) {
            int currentPlayer = mafiaGame.getCurrentPlayer();
            currentPlayer++;
            mafiaGame.setCurrentPlayer(currentPlayer);
            //}
        }
        else {
            int currentPlayer = mafiaGame.getCurrentPlayer();
            currentPlayer++;
            mafiaGame.setCurrentPlayer(currentPlayer);
        }*/

        GameStorage.getInstance().setGame(mafiaGame, number);

        return mafiaGame;
    }

    private void killPlayerAndCheckWinner(MafiaGame mafiaGame, List<Player> players, boolean flag) {
        mafiaGame.setCurrentPlayer(0);
        int playerIndexWithBiggestVoteScore = 0;
        int counter = 0;
        int maxPlayerVoteCount = players.get(0).getVote();
        //int secondMaxPlayerVoteCount = players.get(0).getVote();

        for (int i = 1; i < players.size(); i++) {
            if (players.get(i).getVote() > maxPlayerVoteCount) {
                playerIndexWithBiggestVoteScore = players.get(i).getVote();
                maxPlayerVoteCount = players.get(i).getVote();
            }
        }

        int playerIndex = 0;
        for (Player player : players) {
            if (player.getVote() == playerIndexWithBiggestVoteScore) {
                playerIndex = player.getPosition();
            }
        }

        for (int i = 1; i < players.size(); i++) {
            if (players.get(i).getVote() == maxPlayerVoteCount) {
                counter++;
            }
        }

        if (counter < 2) {
            Player killPlayer;
            /*if (flag)*/
            killPlayer = players.get(playerIndex - 1);
            /*else killPlayer = players.get(playerIndexWithBiggestVoteScore - 1);//todo: fix bag - out of bound exception*/
            killPlayer.setAlive(false);

            if (checkWinner(players) == WinStatus.FAIR_WIN) {
                mafiaGame.setWinStatus(WinStatus.FAIR_WIN);
            } else if (checkWinner(players) == WinStatus.MAFIA_WIN) {
                mafiaGame.setWinStatus(WinStatus.MAFIA_WIN);
            }

            for (Player player : players) {
                player.setVote(0);
            }

            playerRepository.updateFullPlayer(killPlayer);
        }
    }

    //todo: reset lobby after game session && reset votes as well
    private void resetLobby() {
    }

    private WinStatus checkWinner(List<Player> players) {
        int mafiaCount = 0;
        int civilianCount = 0;

        for (Player player : players) {
            if (player.getAlive()) {
                if (player.getRole().equals(PlayerRole.MAFIA.toString())) {
                    mafiaCount++;
                } else if (player.getRole().equals(PlayerRole.CIVILIAN.toString()) && player.getRole().equals(PlayerRole.SHERIFF.toString()) ||
                        player.getRole().equals(PlayerRole.CIVILIAN.toString()) || player.getRole().equals(PlayerRole.SHERIFF.toString())) {
                    civilianCount++;
                }
            }
        }
        //todo: mafia always wins - fix
        if (mafiaCount == civilianCount) {
            return WinStatus.MAFIA_WIN;
        }
        if (mafiaCount == 0) {
            return WinStatus.FAIR_WIN;
        }

        return null;
    }

    private TimerTask setTimer() {
        return new TimerTask() {
            @Override
            public void run() {
                System.out.println("is running");
            }
        };
    }

    private void startTimer(Integer number) {
        MafiaGame gameTimer = GameStorage.getInstance().getMafiaGame(number);

        /*Thread thread = new Thread(new LongRunningTask());
        thread.start();

        Timer timer = new Timer();
        TimeOutTask timeOutTask = new TimeOutTask(thread, timer);
        timer.schedule(timeOutTask, 3000);*/

        Timer timer = gameTimer.getTimer();
        TimerTask timerTask = setTimer();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
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
        Lobby currLobby = lobbyRepository.selectCurrentLobbyByPlayerLobbyId(id);
        Map<Integer, Lobby> lobbyMap = LobbyStorage.getInstance().getLobby();
        lobbyMap.remove(currLobby.getNumber());
        LobbyStorage.getInstance().setLobbies(lobbyMap);
        playerRepository.deleteByLobbyId(id);
        lobbyRepository.delete(id);
    }
}
