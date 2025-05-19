package com.bank.card.services;

import com.bank.card.entities.Account;
import com.bank.card.entities.Card;
import com.bank.card.entities.CardType;
import com.bank.card.kafka.MessageProducer;
import com.bank.card.kafka.models.ProducerEvent;
import com.bank.card.models.AccountModel;
import com.bank.card.models.CardModel;
import com.bank.card.models.ValidationResult;
import com.bank.card.payloads.CardCreateRequest;
import com.bank.card.repositories.AccountDAO;
import com.bank.card.repositories.CardDAO;
import com.bank.card.repositories.CardTypeDAO;
import com.bank.card.services.interfaces.CardService;
import com.bank.card.utils.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
public class CardServiceImpl implements CardService {
    @Value("${card.topic}")
    private String cardTopic;
    private final AccountDAO accountDAO;
    private final CardTypeDAO cardTypeDAO;
    private final CardDAO cardDAO;
    private final MessageProducer messageProducer;

    public CardServiceImpl(AccountDAO accountDAO, CardTypeDAO cardTypeDAO, CardDAO cardDAO, MessageProducer messageProducer) {
        this.accountDAO = accountDAO;
        this.cardTypeDAO = cardTypeDAO;
        this.cardDAO = cardDAO;
        this.messageProducer = messageProducer;
    }

    @Override
    public ResponseData<Object> createUpdate(CardCreateRequest request, UUID id) {
        CardModel cardModel = null;
        ValidationResult validation = validate(request, id);

        if (validation.errors.isEmpty()) {

            Card card = validation.card;
            Account account = validation.account;
            CardType cardType = validation.getCardType();

            if (card != null) {
                card.setDateLastUpdated(LocalDateTime.now());
            } else {
                card = new Card();
                card.setDateCreated(LocalDateTime.now());
            }

            card.setCardType(cardType);
            card.setCardAlias(request.getCardAlias());
            card.setAccount(account);
            card.setCvv(String.valueOf(request.getCvv()));
            card.setPan(request.getPan());
            cardDAO.save(card);
            cardModel = new CardModel(card,true);

            sendCardData(new CardModel(card,false), Constants.Kafka.CREATE_UPDATE);
        }
        return validation.errors.isEmpty() ? new ResponseData<>(Response.SUCCESS.status(), cardModel):new ResponseData<>(Response.ERRORS_OCCURRED.status(),validation.getErrors());
    }

    private void sendCardData(CardModel cardModel, String event) {
        ProducerEvent producerEvent = new ProducerEvent(event,cardModel);
        messageProducer.publish(cardTopic,Util.toJson(producerEvent));
    }

    private ValidationResult validate(CardCreateRequest request, UUID id) {
        ValidationResult result = new ValidationResult();
        CardType cardType = cardTypeDAO.findByCode(request.getCardType().toLowerCase());
        if(cardType == null){
            result.status = Response.CARD_TYPE_NOT_FOUND.status();
            result.errors.add(new FieldError("cardType", result.status.getMessage()));
        }else {
            result.cardType = cardType;
        }

        if (!Util.isValidPan(request.getPan())) {
            result.status = Response.INVALID_PAN.status();
            result.errors.add(new FieldError("pan", result.status.getMessage()));
        }

        if (!Util.isValidCvv(String.valueOf(request.getCvv()))) {
            result.status = Response.INVALID_CVV.status();
            result.errors.add(new FieldError("cvv", result.status.getMessage()));
        }

        result.account = accountDAO.findFirstByIdAndDateDeletedIsNull(request.getAccountId());
        if (result.account == null) {
            result.status = Response.ACCOUNT_NOT_FOUND.status();
            result.errors.add(new FieldError("accountId", result.status.getMessage()));
        }

        if (id != null) {
            result.card = cardDAO.findFirstByIdAndDateDeletedIsNull(id);
            if (result.card == null) {
                result.status = Response.CARD_NOT_FOUND.status();
                result.errors.add(new FieldError("id", result.status.getMessage()));
            }
        }else{
            if(cardType != null) {
                Card card = cardDAO.findFirstByCardTypeAndAccountAndDateDeletedIsNull(cardType, new Account(request.getAccountId()));
                if (card != null) {
                    result.status = Response.CARD_TYPE_ACCOUNT_EXISTS.status();
                    result.status.setMessage(MessageFormat.format(result.getStatus().getMessage(), request.getAccountId(),cardType.getName()));
                    result.errors.add(new FieldError("cardType", result.status.getMessage()));
                }
            }
        }

        return result;
    }


    @Override
    public Object getAll(Pageable pageable, String cardType, String pan, String cardAlias,Boolean unMaskPan) {
        String alias = Util.processSearchName(cardAlias);
        Page<Card> cards = cardDAO.findAllByCardTypeAndPanAndCardAliasAndDateDeletedIsNull(cardType,pan,alias,pageable);
        List<CardModel> cardModels = cards.getContent().stream()
                .map(card -> new CardModel(card, unMaskPan))
                .toList();

        return new PagedResponse<>(
                Response.SUCCESS.status(),
                cardModels,
                cards.getTotalPages(),
                cards.getSize(),
                cards.getTotalElements()
        );
    }

    @Override
    public ResponseData<CardModel> getOne(UUID id,Boolean unMaskPan) {
        ResponseStatus status;
        Card card = cardDAO.findFirstByIdAndDateDeletedIsNull(id);
        CardModel cardModel = null;
        if(card != null){
            cardModel = new CardModel(card,unMaskPan);
            status = Response.SUCCESS.status();
        }else {
            status =Response.CARD_NOT_FOUND.status();
        }
        return new ResponseData<>(status,cardModel);
    }

    @Override
    public ResponseData<UUID> delete(UUID id) {
        ResponseStatus status;
        Card card = cardDAO.findFirstByIdAndDateDeletedIsNull(id);
        if(card !=  null){
            card.setDateDeleted(LocalDateTime.now());
            cardDAO.save(card);
            CardModel cardModel = new CardModel(card,false);
            status = Response.CARD_DELETED.status();

            //Send customer data to other microservices using kafka
            sendCardData(cardModel,Constants.Kafka.DELETE);
        }else {
            status = Response.CARD_NOT_FOUND.status();
        }
        return new ResponseData<>(status,id);
    }

    @Override
    public void deleteAllAccountCards(Account account) {
        List<Card> cards = cardDAO.findAllByAccountAndDateDeletedIsNull(account);
        for (Card card:cards){
            delete(card.getId());
        }
    }
}
