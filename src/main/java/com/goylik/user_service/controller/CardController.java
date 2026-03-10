package com.goylik.user_service.controller;

import com.goylik.user_service.model.dto.request.CreateCardRequest;
import com.goylik.user_service.model.dto.request.UpdateCardRequest;
import com.goylik.user_service.model.dto.response.CardResponse;
import com.goylik.user_service.service.CardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Validated
public class CardController {
    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardService.createCard(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(@Positive @PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAll(pageable));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<CardResponse>> getAllCardsByUserId(@Positive @PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getAllCardsByUserId(userId));
    }

    @PutMapping("/{id}")
    public CardResponse updateCard(@Positive @PathVariable Long id,
                                   @Valid @RequestBody UpdateCardRequest request) {
        return cardService.updateCard(id, request);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCard(@Positive @PathVariable Long id) {
        cardService.activateCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCard(@Positive @PathVariable Long id) {
        cardService.deactivateCard(id);
        return ResponseEntity.noContent().build();

    }
}
