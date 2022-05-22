package org.mafiagame.mafia.model;

import java.util.Objects;

public class ServerStatistics {
    private Integer mafiaWinCount;
    private Integer fairWinCount;
    private Integer totalGameCount;

    public ServerStatistics() {

    }

    public Integer getMafiaWinCount() {
        return mafiaWinCount;
    }

    public void setMafiaWinCount(Integer mafiaWinCount) {
        this.mafiaWinCount = mafiaWinCount;
    }

    public Integer getFairWinCount() {
        return fairWinCount;
    }

    public void setFairWinCount(Integer fairWinCount) {
        this.fairWinCount = fairWinCount;
    }

    public Integer getTotalGameCount() {
        return totalGameCount;
    }

    public void setTotalGameCount(Integer totalGameCount) {
        this.totalGameCount = totalGameCount;
    }

    @Override
    public String toString() {
        return "ServerStatistics{" +
                "mafiaWinCount=" + mafiaWinCount +
                ", fairWinCount=" + fairWinCount +
                ", totalGameCount=" + totalGameCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerStatistics that = (ServerStatistics) o;
        return Objects.equals(mafiaWinCount, that.mafiaWinCount) && Objects.equals(fairWinCount, that.fairWinCount) && Objects.equals(totalGameCount, that.totalGameCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mafiaWinCount, fairWinCount, totalGameCount);
    }
}