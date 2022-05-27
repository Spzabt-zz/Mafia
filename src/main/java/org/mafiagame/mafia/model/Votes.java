package org.mafiagame.mafia.model;

import java.util.Objects;

public class Votes {
    private Integer playerId;
    private Integer candidateId;
    private Integer day;

    public Votes() {

    }

    public Votes(Integer playerId, Integer candidateId, Integer day) {
        this.playerId = playerId;
        this.candidateId = candidateId;
        this.day = day;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Votes votes = (Votes) o;
        return Objects.equals(playerId, votes.playerId) && Objects.equals(candidateId, votes.candidateId) && Objects.equals(day, votes.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, candidateId, day);
    }
}
