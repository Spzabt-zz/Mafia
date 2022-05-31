package org.mafiagame.mafia.exception;

public class InvalidLobbyNumberException extends Exception {
    private final String message;

    public InvalidLobbyNumberException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
