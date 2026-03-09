package com.goylik.user_service.exception.card;

public class InvalidCardNumberException extends RuntimeException {
    public InvalidCardNumberException() {
    }

    public InvalidCardNumberException(String message) {
        super(message);
    }

    public InvalidCardNumberException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCardNumberException(Throwable cause) {
        super(cause);
    }
}
