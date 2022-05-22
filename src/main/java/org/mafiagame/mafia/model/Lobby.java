package org.mafiagame.mafia.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lobby {
    private Integer id;
    private Integer number;
    private Boolean gameStatus;
    private final List<Player> players;

    public Lobby() {
        players = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Boolean getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(Boolean gameStatus) {
        this.gameStatus = gameStatus;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Player player) {
        players.add(player);
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "id=" + id +
                ", number=" + number +
                ", gameStatus=" + gameStatus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lobby lobby = (Lobby) o;
        return Objects.equals(id, lobby.id) && Objects.equals(number, lobby.number) && Objects.equals(gameStatus, lobby.gameStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, gameStatus);
    }
}
