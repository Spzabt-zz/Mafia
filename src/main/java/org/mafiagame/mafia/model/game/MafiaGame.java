package org.mafiagame.mafia.model.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.mafiagame.mafia.model.Player;
import org.mafiagame.mafia.model.enam.DayTime;
import org.mafiagame.mafia.model.enam.Phase;
import org.mafiagame.mafia.model.enam.WinStatus;

import java.util.List;

@Data
public class MafiaGame {
    private Integer day;
    private DayTime dayTime;
    private Phase phase;
    private Integer currentPlayer;
    private List<Player> players;
    private WinStatus winStatus;
    private Boolean timerIsWorking;
    private Boolean playerIsMafia;

    @JsonIgnore
    private GameTimer gameTimer;

    @JsonIgnore
    private Boolean isFirstPlayer;

    @JsonIgnore
    private int localCounter;

    @JsonIgnore
    private Boolean isLastPlayer;
}