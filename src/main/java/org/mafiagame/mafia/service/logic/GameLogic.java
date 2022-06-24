package org.mafiagame.mafia.service.logic;

import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.model.enam.*;
import org.mafiagame.mafia.model.game.MafiaGame;
import org.mafiagame.mafia.repository.LobbyRepository;
import org.mafiagame.mafia.repository.PlayerRepository;
import org.mafiagame.mafia.repository.VotesRepository;
import org.mafiagame.mafia.storage.LobbyStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
public class GameLogic {
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;
    private final VotesRepository votesRepository;

    @Autowired
    public GameLogic(LobbyRepository lobbyRepository, PlayerRepository playerRepository, VotesRepository votesRepository) {
        this.lobbyRepository = lobbyRepository;
        this.playerRepository = playerRepository;
        this.votesRepository = votesRepository;
    }

    public void killPlayerAndCheckWinner(MafiaGame mafiaGame, List<Player> players, Integer number) {
        //mafiaGame.setCurrentPlayer(0);
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

    public void resetLobby(Integer number) {
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

    public WinStatus checkWinner(List<Player> players) {
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

    public Lobby setPlayerStats(Lobby lobby) {
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

    public String getRandomRole(List<String> playerRoles) {
        int randomIndex = 0;
        if (playerRoles.size() != 1) {
            randomIndex = getRandomNumberUsingNextInt(0, playerRoles.size());
        }
        String randomElement = playerRoles.get(randomIndex);
        playerRoles.remove(randomIndex);

        return randomElement;
    }

    public int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public void findCurrentPlayer(Player player, List<Player> players, MafiaGame mafiaGame) {
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
            Player nextPlayer = null;
            //if (mafiaGame.getCurrentPlayer() != player.getPosition());
                nextPlayer = players.get(mafiaGame.getCurrentPlayer());
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

    public Player findLastPlayer(List<Player> players) {
        int maxPlayerPos = players.get(0).getPosition();

        for (Player player : players) {
            if (player.getAlive())
                if (player.getPosition() >= maxPlayerPos) {
                    maxPlayerPos = player.getPosition();
                }
        }
        return players.get(maxPlayerPos - 1);
    }

    public Player findLastMafia(List<Player> players) {
        int maxPlayerPos = players.get(0).getPosition();

        for (Player player : players) {
            if (player.getAlive() && Objects.equals(player.getRole(), PlayerRole.MAFIA.toString()))
                if (player.getPosition() >= maxPlayerPos) {
                    maxPlayerPos = player.getPosition();
                }
        }
        return players.get(maxPlayerPos - 1);
    }

    public void sheriffMove(List<Player> players, MafiaGame mafiaGame) {
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

    public void findCurrentMafia(Player player, List<Player> players, MafiaGame mafiaGame) {
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
}
