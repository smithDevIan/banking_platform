package com.bank.card.services.interfaces;

import com.bank.card.entities.Account;
import com.bank.card.models.CardModel;
import com.bank.card.payloads.CardCreateRequest;
import com.bank.card.utils.ResponseData;
import com.bank.card.utils.ResponseDataExtended;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CardService {
    ResponseData<Object> createUpdate(CardCreateRequest cardCreateRequest, UUID id);

    Object getAll(Pageable pageable, String cardType, String pan, String cardAlias,Boolean unMaskPan);

    ResponseData<CardModel> getOne(UUID id, Boolean unMaskPan);

    ResponseData<UUID> delete(UUID id);

    void deleteAllAccountCards(Account account);
}
