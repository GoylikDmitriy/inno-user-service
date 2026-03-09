package com.goylik.user_service.exception.card;

public class CardLimitExceededException extends RuntimeException {
    public CardLimitExceededException() {
    }

    public CardLimitExceededException(String message) {
        super(message);
    }

    public CardLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardLimitExceededException(Throwable cause) {
        super(cause);
    }
}
