package com.bank.customer.kafka.models;


import com.bank.customer.entities.Card;
import com.bank.customer.utils.ItemCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardModel {
    private UUID id;
    private String cardAlias;
    private ItemCode cardType;
    private AccountModel account;
    private String pan;
    private String cvv;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastUpdated;
    private LocalDateTime dateDeleted;

    public CardModel(Card card, boolean mask) {
        this.id = card.getId();
        this.cardAlias = card.getCardAlias();
        this.cardType = new ItemCode(card.getCardType().getCode(), card.getCardType().getName());
        this.account = new AccountModel(card.getAccount());
        this.dateCreated = card.getDateCreated();
        this.dateLastUpdated = card.getDateLastUpdated();
        this.dateDeleted = card.getDateDeleted();

        // Apply masking if `mask=true`
        this.pan = mask ? maskPan(card.getPan()) : card.getPan();
        this.cvv = "***";
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 8) {
            return pan; // Not enough digits to mask
        }
        String firstFour = pan.substring(0, 4);
        String lastFour = pan.substring(pan.length() - 4);
        return firstFour + "****" + lastFour;
    }

    @JsonIgnore
    public String getRawCvv() {
        return cvv;
    }
    public String getCvv() {
        return "***";
    }
}
