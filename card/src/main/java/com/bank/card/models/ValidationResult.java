package com.bank.card.models;

import com.bank.card.entities.Account;
import com.bank.card.entities.Card;
import com.bank.card.entities.CardType;
import com.bank.card.entities.Customer;
import com.bank.card.utils.FieldError;
import com.bank.card.utils.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResult {
    public List<FieldError> errors = new ArrayList<>();
    public ResponseStatus status;
    public Account account;
    public Card card;
    public CardType cardType;
}
