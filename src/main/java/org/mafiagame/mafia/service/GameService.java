package org.mafiagame.mafia.service;

import org.mafiagame.mafia.exception.InvalidGameException;
import org.mafiagame.mafia.exception.InvalidLobbySizeException;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.model.enam.DayTime;
import org.mafiagame.mafia.model.enam.GameStatus;
import org.mafiagame.mafia.model.enam.Phase;
import org.mafiagame.mafia.model.enam.PlayerRole;
import org.mafiagame.mafia.model.game.GameTimer;
import org.mafiagame.mafia.model.game.MafiaGame;
import org.mafiagame.mafia.repository.LobbyRepository;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.mafiagame.mafia.repository.VotesRepository;
import org.mafiagame.mafia.service.logic.GameLogic;
import org.mafiagame.mafia.storage.GameStorage;
import org.mafiagame.mafia.storage.LobbyStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class GameService {
    private static final int MIN_COUNT_PLAYERS_IN_LOBBY = 4;
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;
    private final VotesRepository votesRepository;
    private final GameLogic gameLogic;

    @Autowired
    public GameService(LobbyRepository lobbyRepository, PlayerRepository playerRepository, VotesRepository votesRepository, GameLogic gameLogic) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
        this.votesRepository = votesRepository;
        this.gameLogic = gameLogic;
    }

    public Lobby startGame(Integer number) throws InvalidLobbySizeException {
        if (LobbyStorage.getInstance().getLobby().get(number).getPlayers().size() < MIN_COUNT_PLAYERS_IN_LOBBY) {
            throw new InvalidLobbySizeException("Min players in lobby " + MIN_COUNT_PLAYERS_IN_LOBBY + ". Invite more people");
        }
        Lobby currLobby = LobbyStorage.getInstance().getLobby().get(number);
        currLobby.setGameStatus(GameStatus.IN_PROGRESS.toString());
        Lobby modifiedLobby = gameLogic.setPlayerStats(currLobby);
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

    public MafiaGame speech(Integer number) throws InterruptedException {
        MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(number);
        GameTimer gameTimer = mafiaGame.getGameTimer();
        List<Player> players = mafiaGame.getPlayers();

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

                if (Objects.equals(mafiaGame.getCurrentPlayer(), gameLogic.findLastPlayer(players).getPosition())) {
                    mafiaGame.setIsLastPlayer(true);
                }

                if (!mafiaGame.getIsLastPlayer()) {
                    mafiaGame.setGameTimer(new GameTimer(number, false));
                    gameTimer.startTimerForSpeech();
                }

                if (!mafiaGame.getIsFirstPlayer() && player.getAlive()) {
                    if (!mafiaGame.getIsLastPlayer()) {
                        gameLogic.findCurrentPlayer(player, players, mafiaGame);
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

                        abstainVote(number);
                        Thread.sleep(1100);
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

    public MafiaGame civilianVoting(Integer number, Integer candidateId) throws InvalidGameException, InterruptedException {
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

            doVote(candidateId, players, player);

            if (Objects.equals(mafiaGame.getCurrentPlayer(), gameLogic.findLastPlayer(players).getPosition())) {
                mafiaGame.setIsLastPlayer(true);
            }

            if (!mafiaGame.getIsLastPlayer()) {
                gameLogic.findCurrentPlayer(player, players, mafiaGame);
            }

            if (mafiaGame.getIsLastPlayer()) {
                mafiaGame.setIsLastPlayer(false);

                for (Player player1 : players) {
                    if (player1.getAlive()) {
                        mafiaGame.setCurrentPlayer(player1.getPosition());
                        break;
                    }
                }

                gameLogic.killPlayerAndCheckWinner(mafiaGame, players, number);
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
                if (Objects.equals(mafiaGame.getCurrentPlayer(), gameLogic.findLastPlayer(players).getPosition())) {
                    mafiaGame.setIsLastPlayer(true);
                }

                if (!mafiaGame.getIsLastPlayer()) {
                    gameLogic.findCurrentPlayer(player, players, mafiaGame);
                }

                if (mafiaGame.getIsLastPlayer()/* && mafiaGame.getTimerForDayVotingIsWorking()*/) {
                    mafiaGame.setIsLastPlayer(false);

                    for (Player player1 : players) {
                        if (player1.getAlive()) {
                            mafiaGame.setCurrentPlayer(player1.getPosition());
                            break;
                        }
                    }

                    gameLogic.killPlayerAndCheckWinner(mafiaGame, players, number);
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
            Thread.sleep(1100);
            mafiaGame.setGameTimer(new GameTimer(number, false));
            gameTimer.startTimerForMafiaTurn();
        }

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

            doVote(candidateId, players, player);

            if (Objects.equals(mafiaGame.getCurrentPlayer(), gameLogic.findLastMafia(players).getPosition())) {
                mafiaGame.setIsLastPlayer(true);
            }

            if (!mafiaGame.getIsLastPlayer()) {
                gameLogic.findCurrentMafia(player, players, mafiaGame);
            }

            if (mafiaGame.getIsLastPlayer()) {
                mafiaGame.setIsLastPlayer(false);

                gameLogic.killPlayerAndCheckWinner(mafiaGame, players, number);
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

                        abstainVote(number);
                        Thread.sleep(1100);
                        mafiaGame.setGameTimer(new GameTimer(number, false));
                        gameTimer.startTimerForSpeech();
                        mafiaGame.setCurrentPlayer(1);
                        break;
                    }
                }
            }
        } else {
            if (!mafiaGame.getTimerForNightMafiaVotingIsWorking()) {
                if (Objects.equals(mafiaGame.getCurrentPlayer(), gameLogic.findLastMafia(players).getPosition())) {
                    mafiaGame.setIsLastPlayer(true);
                }

                if (!mafiaGame.getIsLastPlayer()) {
                    gameLogic.findCurrentMafia(player, players, mafiaGame);
                }

                if (mafiaGame.getIsLastPlayer()) {
                    mafiaGame.setIsLastPlayer(false);

                    gameLogic.killPlayerAndCheckWinner(mafiaGame, players, number);
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

                            abstainVote(number);
                            Thread.sleep(1100);
                            mafiaGame.setGameTimer(new GameTimer(number, false));
                            gameTimer.startTimerForSpeech();
                            mafiaGame.setCurrentPlayer(1);
                            break;
                        }
                    }
                }
            }
        }
        /*if (!mafiaGame.getTimerForNightMafiaVotingIsWorking() && !mafiaGame.getIsLastPlayer()) {
            mafiaGame.setGameTimer(new GameTimer(number, false));
            gameTimer.startTimerForMafiaTurn();
        }*/
        if (mafiaGame.getPhase() == Phase.SHERIFF) {
            abstainVote(number);
            Thread.sleep(1100);
            mafiaGame.setGameTimer(new GameTimer(number, false));
            gameTimer.startTimerForSheriffTurn();
        }

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
                gameLogic.sheriffMove(players, mafiaGame);
            }
        } else {
            if (!mafiaGame.getTimerForSheriffCheckingIsWorking()) {
                if (player.getAlive()) {
                    gameLogic.sheriffMove(players, mafiaGame);
                }
            }
        }

        if (mafiaGame.getPhase() == Phase.SPEECH) {
            abstainVote(number);
            Thread.sleep(1100);
            mafiaGame.setGameTimer(new GameTimer(number, false));
            gameTimer.startTimerForSpeech();
        }

        GameStorage.getInstance().setGame(mafiaGame, number);

        return mafiaGame;
    }

    public MafiaGame getGameStatus(Integer number) {
        return GameStorage.getInstance().getMafiaGame(number);
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

    private void doVote(Integer candidateId, List<Player> players, Player player) {
        Votes votes = player.getVotes();
        votes.setCandidateId(candidateId);
        player.setVotes(votes);
        votesRepository.updatePlayerVotes(votes);

        Player votedPlayer = players.get(candidateId - 1);
        int voteCount = votedPlayer.getVote();
        voteCount++;
        votedPlayer.setVote(voteCount);
        playerRepository.updateFullPlayer(votedPlayer);
    }
}
