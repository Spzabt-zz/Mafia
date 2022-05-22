package org.mafiagame.mafia.model;

import lombok.Data;

import java.util.List;
import java.util.Timer;

@Data
public class MafiaGame {
    private List<Player> players;
    private GameStatus gameStatus;
    private Lobby lobby;
    private Votes votes;
    private Timer timer;
}
