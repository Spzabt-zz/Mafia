package org.mafiagame.mafia.model;

import org.springframework.data.annotation.Id;

public class Player {
    private Integer id;
    private String name;
    private String role;
    private Boolean gameStatus;

    public Player(Integer id, String name, String role, Boolean gameStatus) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.gameStatus = gameStatus;
    }

    public Player() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(Boolean gameStatus) {
        this.gameStatus = gameStatus;
    }
}
