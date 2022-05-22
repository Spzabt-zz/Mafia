package org.mafiagame.mafia.storage;

import org.mafiagame.mafia.model.Lobby;
import org.mafiagame.mafia.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyStorage {
    private static Map<Integer, Lobby> lobbies;
    private static LobbyStorage instance;

    private LobbyStorage() {
        lobbies = new HashMap<>();
    }

    public static synchronized LobbyStorage getInstance() {
        if (instance == null) {
            instance = new LobbyStorage();
        }
        return instance;
    }

    public Map<Integer, Lobby> getLobby() {
        return lobbies;
    }

    public void setLobby(Lobby lobby) {
        lobbies.put(lobby.getNumber(), lobby);
    }

    public List<Player> getPlayers(Integer number) {
        return lobbies.get(number).getPlayers();
    }

    public void setPlayers(/*Player player, */Integer number, Lobby lobby) {
        //lobbies.put(number).setPlayers(player);
        lobbies.put(number, lobby);
    }
}
