package org.mafiagame.mafia.exception;

public class InvalidPlayerNameException extends Exception {
    private final String message;

    public InvalidPlayerNameException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
