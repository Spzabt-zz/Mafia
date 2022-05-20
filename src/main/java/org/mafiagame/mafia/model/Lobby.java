package org.mafiagame.mafia.model;

public class Lobby {
    private Integer id;
    private Integer number;
    private Boolean gameStatus;

    public Lobby() {

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

    @Override
    public String toString() {
        return "Lobby{" +
                "id=" + id +
                ", number=" + number +
                ", gameStatus=" + gameStatus +
                '}';
    }
}
