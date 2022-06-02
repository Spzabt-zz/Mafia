package org.mafiagame.mafia.exception;

public class InvalidGameException extends Exception {
    private final String message;

    public InvalidGameException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
