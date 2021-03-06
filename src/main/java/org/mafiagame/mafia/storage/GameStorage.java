package org.mafiagame.mafia.storage;

import org.mafiagame.mafia.model.game.MafiaGame;

import java.util.HashMap;
import java.util.Map;

public class GameStorage {
    private static Map<Integer, MafiaGame> gameSessions;
    private static GameStorage instance;

    private GameStorage() {
        gameSessions = new HashMap<>();
    }

    public static synchronized GameStorage getInstance() {
        if (instance == null) {
            instance = new GameStorage();
        }
        return instance;
    }

    public MafiaGame getMafiaGame(Integer number) {
        return gameSessions.get(number);
    }

    public Map<Integer, MafiaGame> getGames() {
        return gameSessions;
    }

    public void setGame(MafiaGame game, Integer number) {
        gameSessions.put(number, game);
    }
}
