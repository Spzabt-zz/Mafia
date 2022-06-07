package org.mafiagame.mafia.model.game;

import org.mafiagame.mafia.storage.GameStorage;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {
    private final int gameNumber;
    private int oneMinute = 60;
    private boolean startGameIsWorking = false;
    private boolean speechIsWorking = false;
    private boolean votingIsWorking = false;
    private boolean isAheadOfSchedule;
    private final Timer startTimer;
    private final Timer speechTimer;
    private final Timer voteTimer;

    public GameTimer(int gameNumber, boolean isAheadOfSchedule) {
        this.gameNumber = gameNumber;
        this.isAheadOfSchedule = isAheadOfSchedule;
        startTimer = new Timer();
        speechTimer = new Timer();
        voteTimer = new Timer();
    }

    private final TimerTask timerTaskSeeYourRoles = new TimerTask() {
        int seconds = 0;

        @Override
        public void run() {
            seconds++;
            startGameIsWorking = true;
            MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(gameNumber);
            mafiaGame.setTimerIsWorking(startGameIsWorking);
            if (seconds >= 10) {
                startGameIsWorking = false;
                mafiaGame.setTimerIsWorking(startGameIsWorking);
                //oneMinute = 60;
                startTimer.cancel();
                startTimer.purge();
            }
        }
    };

    public void startTimerForStartGame() {
        startTimer.scheduleAtFixedRate(timerTaskSeeYourRoles, 0, 1000);
    }

    private final TimerTask timerTaskSpeech = new TimerTask() {
        int seconds = 0;

        @Override
        public void run() {

            seconds++;
            speechIsWorking = true;
            MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(gameNumber);
            mafiaGame.setTimerIsWorking(speechIsWorking);
            if (seconds >= oneMinute || mafiaGame.getGameTimer().isAheadOfSchedule()) {
                speechIsWorking = false;
                mafiaGame.setTimerIsWorking(speechIsWorking);
                //oneMinute = 60;
                speechTimer.cancel();
                startTimer.purge();
            }
        }
    };

    public void startTimerForSpeech() {
        speechTimer.scheduleAtFixedRate(timerTaskSpeech, 0, 1000);
    }

    private final TimerTask timerTaskVoting = new TimerTask() {
        int seconds = 0;

        @Override
        public void run() {
            seconds++;
            votingIsWorking = true;
            MafiaGame mafiaGame = GameStorage.getInstance().getMafiaGame(gameNumber);
            mafiaGame.setTimerIsWorking(votingIsWorking);
            if (seconds >= oneMinute || mafiaGame.getGameTimer().isAheadOfSchedule()) {
                votingIsWorking = false;
                mafiaGame.setTimerIsWorking(votingIsWorking);
                voteTimer.cancel();
                voteTimer.purge();
            }
        }
    };

    public void startTimerForVoting() {
        voteTimer.scheduleAtFixedRate(timerTaskVoting, 0, 1000);
    }

    public boolean isStartGameIsWorking() {
        return startGameIsWorking;
    }

    public void setStartGameIsWorking(boolean startGameIsWorking) {
        this.startGameIsWorking = startGameIsWorking;
    }

    public Timer getStartTimer() {
        return startTimer;
    }

    public TimerTask getTimerTaskSeeYourRoles() {
        return timerTaskSeeYourRoles;
    }

    public boolean isSpeechIsWorking() {
        return speechIsWorking;
    }

    public void setSpeechIsWorking(boolean speechIsWorking) {
        this.speechIsWorking = speechIsWorking;
    }

    public boolean isVotingIsWorking() {
        return votingIsWorking;
    }

    public void setVotingIsWorking(boolean votingIsWorking) {
        this.votingIsWorking = votingIsWorking;
    }

    public Timer getSpeechTimer() {
        return speechTimer;
    }

    public Timer getVoteTimer() {
        return voteTimer;
    }

    public boolean isAheadOfSchedule() {
        return isAheadOfSchedule;
    }

    public void setAheadOfSchedule(boolean aheadOfSchedule) {
        isAheadOfSchedule = aheadOfSchedule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameTimer gameTimer = (GameTimer) o;
        return gameNumber == gameTimer.gameNumber && oneMinute == gameTimer.oneMinute && startGameIsWorking == gameTimer.startGameIsWorking && speechIsWorking == gameTimer.speechIsWorking && votingIsWorking == gameTimer.votingIsWorking && isAheadOfSchedule == gameTimer.isAheadOfSchedule && Objects.equals(startTimer, gameTimer.startTimer) && Objects.equals(speechTimer, gameTimer.speechTimer) && Objects.equals(voteTimer, gameTimer.voteTimer) && Objects.equals(timerTaskSeeYourRoles, gameTimer.timerTaskSeeYourRoles) && Objects.equals(timerTaskSpeech, gameTimer.timerTaskSpeech) && Objects.equals(timerTaskVoting, gameTimer.timerTaskVoting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameNumber, oneMinute, startGameIsWorking, speechIsWorking, votingIsWorking, isAheadOfSchedule, startTimer, speechTimer, voteTimer, timerTaskSeeYourRoles, timerTaskSpeech, timerTaskVoting);
    }
}
