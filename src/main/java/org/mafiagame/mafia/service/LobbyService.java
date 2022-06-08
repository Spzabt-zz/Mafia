package org.mafiagame.mafia.service;

import org.mafiagame.mafia.controller.dto.CreateLobbyRequest;
import org.mafiagame.mafia.exception.*;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.model.enam.*;
import org.mafiagame.mafia.model.game.GameTimer;
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

    public Lobby startGame(Integer number) throws InvalidLobbySizeException {
        if (LobbyStorage.getInstance().getLobby().get(number).getPlayers().size() < MIN_COUNT_PLAYERS_IN_LOBBY) {
            throw new InvalidLobbySizeException("Min players in lobby " + MIN_COUNT_PLAYERS_IN_LOBBY + ". Invite more people");
        }
        Lobby currLobby = LobbyStorage.getInstance().getLobby().get(number);
        currLobby.setGameStatus(GameStatus.IN_PROGRESS.toString());
        Lobby modifiedLobby = setPlayerStats(currLobby);
        LobbyStorage.getInstance().setLobby(modifiedLobby);
        lobbyRepository.updateLobbyStatus(number, GameStatus.IN_PROGRESS.toString());

        MafiaGame mafiaGame = new MafiaGame();
        mafiaGame.setDay(1);
        mafiaGame.setDayTime(DayTime.DAY);
        mafiaGame.setPhase(Phase.SPEECH);
        mafiaGame.setCurrentPlayer(1);
        mafiaGame.setPlayers(modifiedLobby.getPlayers());
        mafiaGame.setGameTimer(new GameTimer(number, false));
        mafiaGame.setIsFirstPlayer(true);
        mafiaGame.setIsLastPlayer(false);
        mafiaGame.setPlayerIsMafia(false);

        mafiaGame.setTimerForSpeechIsWorking(false);
        mafiaGame.setTimerForDayVotingIsWorking(false);
        mafiaGame.setTimerForNightMafiaVotingIsWorking(false);
        mafiaGame.setTimerForSheriffCheckingIsWorking(false);

        GameTimer timer = mafiaGame.getGameTimer();
        timer.startTimerForStartGame();

        GameStorage.getInstance().setGame(mafiaGame, modifiedLobby.getNumber());

        return currLobby;
    }

    private Lobby setPlayerStats(Lobby lobby) {
        int count = 0;
        int playerCount = lobby.getPlayers().size();
        List<String> listOfPlayerRoles = null;

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

    public MafiaGame dayVoting(Integer number, Integer candidateId) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);

        mafiaGame = civilianVoting(number, candidateId);

        GameStorage.getInstance().setGame(mafiaGame, number);

        return mafiaGame;
    }

    public MafiaGame civilianVoting(Integer number, Integer candidateId/*, boolean candidateIsNull*/) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);
        List<Player> players = mafiaGame.getPlayers();
        Player player = players.get(mafiaGame.getCurrentPlayer() - 1);
        GameTimer gameTimer = mafiaGame.getGameTimer();

        if (candidateId != null && mafiaGame.getTimerForDayVotingIsWorking()) {
            if (candidateId < 1 || candidateId > players.size()) {
                throw new InvalidGameException("Candidate not found");
            }
            for (Player player1 : players) {
                if (Objects.equals(player1.getPosition(), candidateId) && !player1.getAlive()) {
                    throw new InvalidGameException("Player: " + player1.getName() + " at position " + candidateId + " already killed");
                }
            }
            if (Objects.equals(mafiaGame.getCurrentPlayer(), candidateId)) {
                throw new InvalidGameException("Player can't vote for himself");
            }

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

            if (Objects.equals(mafiaGame.getCurrentPlayer(), findLastPlayer(players).getPosition())) {
                mafiaGame.setIsLastPlayer(true);
            }

            if (!mafiaGame.getIsLastPlayer()) {
                findCurrentPlayer(player, players, mafiaGame);
            }

            if (mafiaGame.getIsLastPlayer()) {
                mafiaGame.setIsLastPlayer(false);

                for (Player player1 : players) {
                    if (player1.getAlive()) {
                        mafiaGame.setCurrentPlayer(player1.getPosition());
                        break;
                    }
                }

                killPlayerAndCheckWinner(mafiaGame, players, number);
                mafiaGame.setDayTime(DayTime.NIGHT);
                mafiaGame.setPhase(Phase.MAFIA);
                for (Player player1 : players) {
                    if (Objects.equals(player1.getRole(), PlayerRole.MAFIA.toString()) && player1.getAlive()) {
                        mafiaGame.setCurrentPlayer(player1.getPosition());
                        break;
                    }
                }
            }
        } else {
            if (!mafiaGame.getTimerForDayVotingIsWorking()) {
                if (Objects.equals(mafiaGame.getCurrentPlayer(), findLastPlayer(players).getPosition())) {
                    mafiaGame.setIsLastPlayer(true);
                }

                if (!mafiaGame.getIsLastPlayer()) {
                    findCurrentPlayer(player, players, mafiaGame);
                }

                if (mafiaGame.getIsLastPlayer()/* && mafiaGame.getTimerForDayVotingIsWorking()*/) {
                    mafiaGame.setIsLastPlayer(false);

                    for (Player player1 : players) {
                        if (player1.getAlive()) {
                            mafiaGame.setCurrentPlayer(player1.getPosition());
                            break;
                        }
                    }

                    killPlayerAndCheckWinner(mafiaGame, players, number);
                    mafiaGame.setDayTime(DayTime.NIGHT);
                    mafiaGame.setPhase(Phase.MAFIA);
                    for (Player player1 : players) {
                        if (Objects.equals(player1.getRole(), PlayerRole.MAFIA.toString()) && player1.getAlive()) {
                            mafiaGame.setCurrentPlayer(player1.getPosition());
                            break;
                        }
                    }
                }
            }
        }
        //mafiaGame.setIsLastPlayer(false);
        if (!mafiaGame.getTimerForDayVotingIsWorking() && !mafiaGame.getIsLastPlayer()) {
            mafiaGame.setGameTimer(new GameTimer(number, false));
            gameTimer.startTimerForVoting();
        }
        if (mafiaGame.getPhase() == Phase.MAFIA) {
            abstainVote(number);
            Thread.sleep(1000);
            mafiaGame.setGameTimer(new GameTimer(number, false));
            gameTimer.startTimerForMafiaTurn();
        }

        GameStorage.getInstance().setGame(mafiaGame, number);

        return mafiaGame;
    }

    private void killPlayerAndCheckWinner(MafiaGame mafiaGame, List<Player> players, Integer number) {
        mafiaGame.setCurrentPlayer(0);
        int playerIndexWithBiggestVoteScore = 0;
        int counter = 0;
        int maxPlayerVoteCount = players.get(0).getVote();

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getVote() >= maxPlayerVoteCount) {
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

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getVote() == maxPlayerVoteCount) {
                counter++;
            }
        }

        if (counter < 2) {
            Player killPlayer;
            killPlayer = players.get(playerIndex - 1);
            killPlayer.setAlive(false);

            Lobby lobby = LobbyStorage.getInstance().getLobby().get(number);
            if (checkWinner(players) == WinStatus.FAIR_WIN) {
                mafiaGame.setWinStatus(WinStatus.FAIR_WIN);
                lobby.setGameStatus(GameStatus.FINISHED.toString());
                LobbyStorage.getInstance().setLobby(lobby);
                lobbyRepository.updateLobbyStatus(number, GameStatus.FINISHED.toString());
                resetLobby(number);
            } else if (checkWinner(players) == WinStatus.MAFIA_WIN) {
                mafiaGame.setWinStatus(WinStatus.MAFIA_WIN);
                lobby.setGameStatus(GameStatus.FINISHED.toString());
                LobbyStorage.getInstance().setLobby(lobby);
                lobbyRepository.updateLobbyStatus(number, GameStatus.FINISHED.toString());
                resetLobby(number);
            }

            for (Player player : players) {
                player.setVote(0);
            }

            playerRepository.updateFullPlayer(killPlayer);
        } else {
            for (Player player : players) {
                player.setVote(0);
            }
        }
    }

    private void resetLobby(Integer number) {
        Lobby lobby = LobbyStorage.getInstance().getLobby().get(number);
        if (Objects.equals(lobby.getGameStatus(), GameStatus.FINISHED.toString())) {
            List<Player> players = lobby.getPlayers();
            for (Player player : players) {
                player.setRole(PlayerRole.DEFAULT.toString());
                player.setAlive(false);
                player.setPosition(0);
                player.setCandidate(false);
                player.setVote(0);
                player.setVotes(null);
                playerRepository.updateFullPlayer(player);
            }
            lobby.setPlayersList(players);
            LobbyStorage.getInstance().setLobby(lobby);
        }
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
        if (mafiaCount == civilianCount) {
            return WinStatus.MAFIA_WIN;
        }
        if (mafiaCount == 0) {
            return WinStatus.FAIR_WIN;
        }

        return null;
    }

    public MafiaGame speech(Integer number) throws InvalidGameException {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);
        GameTimer gameTimer = mafiaGame.getGameTimer();
        List<Player> players = mafiaGame.getPlayers();

        /*if (gameTimer.isStartGameIsWorking()) {
            throw new InvalidGameException("Wait till players finish their card research");
        }
        if (mafiaGame.getTimerForSpeechIsWorking()) {
            throw new InvalidGameException("Wait till player " + mafiaGame.getCurrentPlayer() + " finish his speech");
        }*/
        //if (!gameTimer.isStartGameIsWorking())
        if (!gameTimer.isStartGameIsWorking()) {
            if (!mafiaGame.getTimerForSpeechIsWorking()) {
                Player player = players.get(mafiaGame.getCurrentPlayer() - 1);

                int counter = 0;
                while (!player.getAlive()) {
                    if (player.getPosition() != players.size()) {
                        player = players.get(mafiaGame.getCurrentPlayer() + counter);
                        counter++;
                    } else break;
                }

                if (mafiaGame.getPhase() != Phase.SPEECH) {
                    mafiaGame.setDayTime(DayTime.DAY);
                    mafiaGame.setPhase(Phase.SPEECH);
                }

                if (Objects.equals(mafiaGame.getCurrentPlayer(), findLastPlayer(players).getPosition())) {
                    mafiaGame.setIsLastPlayer(true);
                }

                if (!mafiaGame.getIsLastPlayer()) {
                    mafiaGame.setGameTimer(new GameTimer(number, false));
                    gameTimer.startTimerForSpeech();
                }

                if (!mafiaGame.getIsFirstPlayer() && player.getAlive()) {
                    if (!mafiaGame.getIsLastPlayer()) {
                        findCurrentPlayer(player, players, mafiaGame);
                    }
                    if (mafiaGame.getIsLastPlayer()) {
                        mafiaGame.setIsFirstPlayer(true);
                        mafiaGame.setIsLastPlayer(false);
                        mafiaGame.setPhase(Phase.VOTING);

                        for (Player player1 : players) {
                            if (player1.getAlive()) {
                                mafiaGame.setCurrentPlayer(player1.getPosition());
                                break;
                            }
                        }

                        mafiaGame.setGameTimer(new GameTimer(number, false));
                        gameTimer.startTimerForVoting();

                        GameStorage.getInstance().setGame(mafiaGame, number);
                        return mafiaGame;
                    }
                }

                mafiaGame.setIsLastPlayer(false);
                mafiaGame.setIsFirstPlayer(false);

                GameStorage.getInstance().setGame(mafiaGame, number);
            }
        }

        return mafiaGame;
    }

    public MafiaGame nightMafiaVoting(Integer number, Integer candidateId) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);

        mafiaGame = mafiaTurn(number, candidateId);

        GameStorage.getInstance().setGame(mafiaGame, number);

        return mafiaGame;
    }

    public MafiaGame mafiaTurn(Integer number, Integer candidateId) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);
        List<Player> players = mafiaGame.getPlayers();
        Player player = players.get(mafiaGame.getCurrentPlayer() - 1);
        GameTimer gameTimer = mafiaGame.getGameTimer();

        if (candidateId != null && mafiaGame.getTimerForNightMafiaVotingIsWorking()) {
            if (candidateId < 1 || candidateId > players.size()) {
                throw new InvalidGameException("Candidate not found");
            }
            for (Player player1 : players) {
                if (Objects.equals(player1.getPosition(), candidateId) && !player1.getAlive()) {
                    throw new InvalidGameException("Player: " + player1.getName() + " at position " + candidateId + " already killed");
                }
            }
            if (Objects.equals(mafiaGame.getCurrentPlayer(), candidateId)) {
                throw new InvalidGameException("Mafia can't kill for himself");
            }


            int counter = 0;
            while (!player.getAlive() && !Objects.equals(player.getRole(), PlayerRole.MAFIA.toString())) {
                if (player.getPosition() != players.size()) {
                    player = players.get(mafiaGame.getCurrentPlayer() + counter);
                    counter++;
                } else break;
            }

            if (mafiaGame.getPhase() != Phase.MAFIA) {
                mafiaGame.setDayTime(DayTime.NIGHT);
                mafiaGame.setPhase(Phase.MAFIA);
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

            if (Objects.equals(mafiaGame.getCurrentPlayer(), findLastMafia(players).getPosition())) {
                mafiaGame.setIsLastPlayer(true);
            }

            if (!mafiaGame.getIsLastPlayer()) {
                findCurrentMafia(player, players, mafiaGame);
            }

            if (mafiaGame.getIsLastPlayer()) {
                mafiaGame.setIsLastPlayer(false);

                killPlayerAndCheckWinner(mafiaGame, players, number);
                for (Player player1 : players) {
                    if (Objects.equals(player1.getRole(), PlayerRole.SHERIFF.toString()) && player1.getAlive()) {
                        mafiaGame.setCurrentPlayer(player1.getPosition());
                        break;
                    }
                }

                mafiaGame.setPhase(Phase.SHERIFF);

                for (Player player1 : players) {
                    if (Objects.equals(player1.getRole(), PlayerRole.SHERIFF.toString()) && !player1.getAlive()) {
                        mafiaGame.setDayTime(DayTime.DAY);
                        mafiaGame.setDay(mafiaGame.getDay() + 1);
                        mafiaGame.setPhase(Phase.SPEECH);
                        findCurrentPlayer(player1, players, mafiaGame);
                        break;
                    }
                }
            }
        } else {
            if (!mafiaGame.getTimerForNightMafiaVotingIsWorking()) {
                if (Objects.equals(mafiaGame.getCurrentPlayer(), findLastMafia(players).getPosition())) {
                    mafiaGame.setIsLastPlayer(true);
                }

                if (!mafiaGame.getIsLastPlayer()) {
                    findCurrentMafia(player, players, mafiaGame);
                }

                if (mafiaGame.getIsLastPlayer()) {
                    mafiaGame.setIsLastPlayer(false);

                    killPlayerAndCheckWinner(mafiaGame, players, number);
                    for (Player player1 : players) {
                        if (Objects.equals(player1.getRole(), PlayerRole.SHERIFF.toString()) && player1.getAlive()) {
                            mafiaGame.setCurrentPlayer(player1.getPosition());
                            break;
                        }
                    }

                    mafiaGame.setPhase(Phase.SHERIFF);

                    for (Player player1 : players) {
                        if (Objects.equals(player1.getRole(), PlayerRole.SHERIFF.toString()) && !player1.getAlive()) {
                            mafiaGame.setDayTime(DayTime.DAY);
                            mafiaGame.setDay(mafiaGame.getDay() + 1);
                            mafiaGame.setPhase(Phase.SPEECH);
                            findCurrentPlayer(player1, players, mafiaGame);
                            break;
                        }
                    }
                }
            }
        }
        if (!mafiaGame.getTimerForNightMafiaVotingIsWorking() && !mafiaGame.getIsLastPlayer()) {
            mafiaGame.setGameTimer(new GameTimer(number, false));
            gameTimer.startTimerForMafiaTurn();
        }
        if (mafiaGame.getPhase() == Phase.SHERIFF) {
            abstainVote(number);
            Thread.sleep(1000);
            mafiaGame.setGameTimer(new GameTimer(number, false));
            gameTimer.startTimerForSheriffTurn();
        }

        GameStorage.getInstance().setGame(mafiaGame, number);

        return mafiaGame;
    }

    public MafiaGame nightSheriffVoting(Integer number, Integer candidateId) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);

        mafiaGame = sheriffTurn(number, candidateId);

        GameStorage.getInstance().setGame(mafiaGame, number);

        return mafiaGame;
    }

    public MafiaGame sheriffTurn(Integer number, Integer candidateId) throws InvalidGameException, InterruptedException {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);
        List<Player> players = mafiaGame.getPlayers();
        Player player = players.get(mafiaGame.getCurrentPlayer() - 1);
        GameTimer gameTimer = mafiaGame.getGameTimer();

        if (candidateId != null && mafiaGame.getTimerForSheriffCheckingIsWorking()) {
            if (candidateId < 1 || candidateId > players.size()) {
                throw new InvalidGameException("Candidate not found");
            }
            for (Player player1 : players) {
                if (Objects.equals(player1.getPosition(), candidateId) && !player1.getAlive()) {
                    throw new InvalidGameException("Player: " + player1.getName() + " at position " + candidateId + " already killed");
                }
            }
            if (Objects.equals(mafiaGame.getCurrentPlayer(), candidateId)) {
                throw new InvalidGameException("Sheriff can't check for himself");
            }

            if (mafiaGame.getPhase() != Phase.SHERIFF) {
                mafiaGame.setDayTime(DayTime.NIGHT);
                mafiaGame.setPhase(Phase.SHERIFF);
            }

            if (player.getAlive()) {
                mafiaGame.setPlayerIsMafia(Objects.equals(players.get(candidateId - 1).getRole(), PlayerRole.MAFIA.toString()));
                sheriffMove(players, mafiaGame);
            }
        } else {
            if (!mafiaGame.getTimerForSheriffCheckingIsWorking()) {
                if (player.getAlive()) {
                    sheriffMove(players, mafiaGame);
                }
            }
        }

        if (mafiaGame.getPhase() == Phase.SPEECH) {
            abstainVote(number);
            Thread.sleep(1000);
            mafiaGame.setGameTimer(new GameTimer(number, false));
            gameTimer.startTimerForSpeech();
        }

        GameStorage.getInstance().setGame(mafiaGame, number);

        return mafiaGame;
    }

    public void abstainVote(Integer number) {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);
        GameTimer gameTimer = mafiaGame.getGameTimer();
        gameTimer.setAheadOfSchedule(true);
    }

    public void skipSpeech(Integer number) {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);
        GameTimer gameTimer = mafiaGame.getGameTimer();
        gameTimer.setAheadOfSchedule(true);
        mafiaGame.setGameTimer(gameTimer);
        GameStorage.getInstance().setGame(mafiaGame, number);
    }

    private void sheriffMove(List<Player> players, MafiaGame mafiaGame) {
        mafiaGame.setDayTime(DayTime.DAY);
        mafiaGame.setDay(mafiaGame.getDay() + 1);
        mafiaGame.setPhase(Phase.SPEECH);
        for (Player player1 : players) {
            if (player1.getAlive()) {
                mafiaGame.setCurrentPlayer(player1.getPosition());
                break;
            }
        }
    }

    private void findCurrentMafia(Player player, List<Player> players, MafiaGame mafiaGame) {
        boolean nextMafia = false;
        int playerPos = 0;
        if (player.getPosition() == players.size()) {
            for (Player player1 : players) {
                if (player1.getAlive() && Objects.equals(player1.getRole(), PlayerRole.MAFIA.toString())) {
                    playerPos = player1.getPosition();
                    break;
                }
            }
            mafiaGame.setCurrentPlayer(playerPos);
        } else {
            for (int i = player.getPosition(); i < players.size(); i++) {
                if (players.get(i).getAlive() && Objects.equals(players.get(i).getRole(), PlayerRole.MAFIA.toString())) {
                    mafiaGame.setCurrentPlayer(players.get(i).getPosition());
                    nextMafia = true;
                    break;
                }
                if (i == players.size() - 1 && !nextMafia) {
                    for (Player player1 : players) {
                        if (player1.getAlive() && Objects.equals(player1.getRole(), PlayerRole.MAFIA.toString())) {
                            playerPos = player1.getPosition();
                            break;
                        }
                    }
                    mafiaGame.setCurrentPlayer(playerPos);
                }
            }

        }
    }

    private void findCurrentPlayer(Player player, List<Player> players, MafiaGame mafiaGame) {
        int currentPlayer = 0;
        int playerPos = 0;
        if (player.getPosition() == players.size()) {
            for (Player player1 : players) {
                if (player1.getAlive()) {
                    playerPos = player1.getPosition();
                    break;
                }
            }
            mafiaGame.setCurrentPlayer(playerPos);
        } else {
            int deadCounter = 1;
            Player nextPlayer = players.get(mafiaGame.getCurrentPlayer());
            if (!nextPlayer.getAlive())
                for (int i = player.getPosition(); i < players.size(); i++) {
                    if (!players.get(i).getAlive()) {
                        if (players.get(i).getPosition() != players.size()) {
                            if (players.get(i + 1).getAlive()) {
                                deadCounter++;
                                if (!players.get(i).getAlive()) {
                                    break;
                                }
                                break;
                            } else {
                                deadCounter++;
                            }
                        }
                    }
                }
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
    }

    private Player findLastPlayer(List<Player> players) {
        int maxPlayerPos = players.get(0).getPosition();

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getAlive())
                if (players.get(i).getPosition() >= maxPlayerPos) {
                    maxPlayerPos = players.get(i).getPosition();
                }
        }
        return players.get(maxPlayerPos - 1);
    }

    private Player findLastMafia(List<Player> players) {
        int maxPlayerPos = players.get(0).getPosition();

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getAlive() && Objects.equals(players.get(i).getRole(), PlayerRole.MAFIA.toString()))
                if (players.get(i).getPosition() >= maxPlayerPos) {
                    maxPlayerPos = players.get(i).getPosition();
                }
        }
        return players.get(maxPlayerPos - 1);
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
