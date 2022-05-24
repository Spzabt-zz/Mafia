package org.mafiagame.mafia.exception;

public class InvalidLobbySizeException extends Exception {
    private final String message;

    public InvalidLobbySizeException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
