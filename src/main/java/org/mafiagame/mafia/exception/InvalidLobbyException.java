package org.mafiagame.mafia.exception;

public class InvalidLobbyException extends Exception {
    private final String message;

    public InvalidLobbyException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
