package org.mafiagame.mafia.model.game;

import lombok.Data;
import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Votes;
import org.mafiagame.mafia.model.enam.Phase;

import java.util.List;
import java.util.Timer;

@Data
public class MafiaGame {
    private Integer day;
    private Phase phase;
    private Lobby lobby;
    //private Votes votes;
    //private Timer timer;
}