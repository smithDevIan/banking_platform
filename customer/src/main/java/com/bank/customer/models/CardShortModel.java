package com.bank.customer.models;

import com.bank.customer.entities.Card;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardShortModel {
    private UUID id;
    private String cardAlias;
    private String pan;
    private String cvv;
    private String cardType;

    public CardShortModel(Card card, boolean mask) {
        this.id = card.getId();
        this.cardAlias = card.getCardAlias();
        this.cardType = card.getCardType().getName();

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
}
