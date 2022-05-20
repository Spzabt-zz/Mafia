package org.mafiagame.mafia.model;

public class Votes {
    private Integer playerId;
    private Integer candidateId;
    private Integer day;

    public Votes() {

    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Integer candidateId) {
        this.candidateId = candidateId;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "Votes{" +
                "playerId=" + playerId +
                ", candidateId=" + candidateId +
                ", day=" + day +
                '}';
    }
}
