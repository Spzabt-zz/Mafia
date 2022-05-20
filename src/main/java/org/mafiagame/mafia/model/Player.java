package org.mafiagame.mafia.model;

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

//    public static Player toModel(PlayerEntity entity) {
//        Player model = new Player();
//        model.setId(entity.getId());
//        model.setName(entity.getName());
//        model.setRole(entity.getRole());
//        return model;
//    }

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
}
