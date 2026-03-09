package com.goylik.user_service.exception.card;

public class CardCryptoException extends RuntimeException {
    public CardCryptoException() {
    }

    public CardCryptoException(String message) {
        super(message);
    }

    public CardCryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardCryptoException(Throwable cause) {
        super(cause);
    }
}
