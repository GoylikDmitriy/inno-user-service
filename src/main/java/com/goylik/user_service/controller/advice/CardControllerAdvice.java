package com.goylik.user_service.controller.advice;

import com.goylik.user_service.exception.card.CardCryptoException;
import com.goylik.user_service.exception.card.CardLimitExceededException;
import com.goylik.user_service.exception.card.CardNotFoundException;
import com.goylik.user_service.exception.card.InvalidCardNumberException;
import com.goylik.user_service.model.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class CardControllerAdvice {
    @ExceptionHandler(CardNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCardNotFoundException(CardNotFoundException ex) {
        log.warn("Card not found: {}", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Card not found",
                Map.of("message", ex.getMessage())
        );
    }

    @ExceptionHandler(CardLimitExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCardLimitExceededException(CardLimitExceededException ex) {
        log.warn("Card limit exceeded: {}", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Card limit exceeded",
                Map.of("message", ex.getMessage())
        );
    }

    @ExceptionHandler(InvalidCardNumberException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidCardNumberException(InvalidCardNumberException ex) {
        log.warn("Invalid card number: {}", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid card number",
                Map.of("message", ex.getMessage())
        );
    }

    @ExceptionHandler(CardCryptoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCardCryptoException(CardCryptoException ex) {
        log.warn("Card crypto exception: {}", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Card crypto exception",
                Map.of("message", ex.getMessage())
        );
    }
}
