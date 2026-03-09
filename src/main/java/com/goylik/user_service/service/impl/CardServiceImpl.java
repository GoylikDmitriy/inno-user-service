package com.goylik.user_service.service.impl;

import com.goylik.user_service.exception.card.CardLimitExceededException;
import com.goylik.user_service.exception.card.CardNotFoundException;
import com.goylik.user_service.exception.card.InvalidCardNumberException;
import com.goylik.user_service.mapper.CardMapper;
import com.goylik.user_service.model.dto.request.CreateCardRequest;
import com.goylik.user_service.model.dto.request.UpdateCardRequest;
import com.goylik.user_service.model.dto.response.CardResponse;
import com.goylik.user_service.model.entity.PaymentCard;
import com.goylik.user_service.repository.PaymentCardRepository;
import com.goylik.user_service.service.CardCryptoService;
import com.goylik.user_service.service.CardService;
import com.goylik.user_service.util.CardNumberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final PaymentCardRepository cardRepository;
    private final CardCryptoService cardCryptoService;
    private final CardMapper cardMapper;

    @Value(value = "${app.payment-card.limit-per-user:5}")
    private int cardLimitPerUser;

    @Override
    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        validateUserCardLimitOrThrow(request.userId());
        validateCardNumberOrThrow(request.number());

        var card = cardMapper.toEntity(request);
        card.setNumber(cardCryptoService.encrypt(request.number()));
        card.setActive(true);

        var savedCard = cardRepository.save(card);
        return decryptCardNumberAndMapToResponse(savedCard);
    }

    private void validateUserCardLimitOrThrow(Long userId) {
        if (cardRepository.countByUserId(userId) >= cardLimitPerUser) {
            throw new CardLimitExceededException("User cannot have more than " + cardLimitPerUser + " cards.");
        }
    }

    private void validateCardNumberOrThrow(String cardNumber) {
        if (!CardNumberUtils.validate(cardNumber)) {
            throw new InvalidCardNumberException("Card number is invalid");
        }
    }

    private CardResponse decryptCardNumberAndMapToResponse(PaymentCard card) {
        String decryptedNumber = cardCryptoService.decrypt(card.getNumber());
        return cardMapper.toResponse(card, decryptedNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id) {
        var card = fetchCardByIdOrThrow(id);
        return decryptCardNumberAndMapToResponse(card);
    }

    private PaymentCard fetchCardByIdOrThrow(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getAll(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(this::decryptCardNumberAndMapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getAllCardsByUserId(Long userId) {
        return cardRepository.findByUserId(userId)
                .stream()
                .map(this::decryptCardNumberAndMapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public CardResponse updateCard(Long id, UpdateCardRequest request) {
        var card = fetchCardByIdOrThrow(id);
        cardMapper.updateCardFromDto(request, card);

        if (request.number() != null) {
            validateCardNumberOrThrow(request.number());

            card.setNumber(cardCryptoService.encrypt(request.number()));
        }

        var savedCard = cardRepository.save(card);
        return decryptCardNumberAndMapToResponse(savedCard);
    }

    @Override
    @Transactional
    public void activateCard(Long id) {
        setActiveStatus(id, true);
    }

    @Override
    @Transactional
    public void deactivateCard(Long id) {
        setActiveStatus(id, false);
    }

    private void setActiveStatus(Long id, boolean activeStatus) {
        var card = fetchCardByIdOrThrow(id);
        card.setActive(activeStatus);
    }
}
