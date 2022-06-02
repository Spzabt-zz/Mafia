package org.mafiagame.mafia.model.game;

import lombok.Data;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.model.enam.DayTime;
import org.mafiagame.mafia.model.enam.Phase;
import org.mafiagame.mafia.model.enam.WinStatus;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Data
public class MafiaGame {
    private Integer day;
    private DayTime dayTime;
    private Phase phase;
    private Integer currentPlayer;
    private List<Player> players;
    private WinStatus winStatus;
    private Timer timer;
    //private TimerTask timerTask;

    //private Lobby lobby;
    //private Votes votes;
}