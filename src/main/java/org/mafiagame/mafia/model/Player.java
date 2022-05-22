package org.mafiagame.mafia.model;

import java.util.Objects;

public class Player {
    private Integer id;
    private String name;
    private String role;
    private Boolean alive;
    private Integer position;
    private Boolean candidate;
    private Integer vote;
    private Boolean admin;
    private Integer lobbyId;

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

    public Boolean getAlive() {
        return alive;
    }

    public void setAlive(Boolean alive) {
        this.alive = alive;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getCandidate() {
        return candidate;
    }

    public void setCandidate(Boolean candidate) {
        this.candidate = candidate;
    }

    public Integer getVote() {
        return vote;
    }

    public void setVote(Integer vote) {
        this.vote = vote;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Integer getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Integer lobbyId) {
        this.lobbyId = lobbyId;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", alive=" + alive +
                ", position=" + position +
                ", candidate=" + candidate +
                ", vote=" + vote +
                ", admin=" + admin +
                ", lobbyId=" + lobbyId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id) && Objects.equals(name, player.name) && Objects.equals(role, player.role) && Objects.equals(alive, player.alive) && Objects.equals(position, player.position) && Objects.equals(candidate, player.candidate) && Objects.equals(vote, player.vote) && Objects.equals(admin, player.admin) && Objects.equals(lobbyId, player.lobbyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, role, alive, position, candidate, vote, admin, lobbyId);
    }
}
