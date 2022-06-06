package org.mafiagame.mafia.model.game;

import org.mafiagame.mafia.storage.GameStorage;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {
    private final int gameNumber;
    //private int seconds = 0;
    private boolean startGameIsWorking = false;
    private boolean speechIsWorking = false;
    private boolean votingIsWorking = false;
    private final Timer startTimer;
    private final Timer speechTimer;
    private final Timer voteTimer;

    public GameTimer(int gameNumber) {
        this.gameNumber = gameNumber;
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
                startTimer.cancel();
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
            if (seconds >= 10) {
                speechIsWorking = false;
                mafiaGame.setTimerIsWorking(speechIsWorking);
                speechTimer.cancel();
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
            if (seconds >= 60) {
                votingIsWorking = false;
                mafiaGame.setTimerIsWorking(votingIsWorking);
                voteTimer.cancel();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameTimer gameTimer = (GameTimer) o;
        return gameNumber == gameTimer.gameNumber && startGameIsWorking == gameTimer.startGameIsWorking && speechIsWorking == gameTimer.speechIsWorking && votingIsWorking == gameTimer.votingIsWorking && Objects.equals(startTimer, gameTimer.startTimer) && Objects.equals(speechTimer, gameTimer.speechTimer) && Objects.equals(voteTimer, gameTimer.voteTimer) && Objects.equals(timerTaskSeeYourRoles, gameTimer.timerTaskSeeYourRoles) && Objects.equals(timerTaskSpeech, gameTimer.timerTaskSpeech);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameNumber, startGameIsWorking, speechIsWorking, votingIsWorking, startTimer, speechTimer, voteTimer, timerTaskSeeYourRoles, timerTaskSpeech);
    }
}
