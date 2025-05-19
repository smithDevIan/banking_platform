package com.bank.customer.services.interfaces;


import com.bank.customer.kafka.models.AccountModel;
import com.bank.customer.kafka.models.CardModel;

public interface KafkaService {
    void createUpdateAccount(AccountModel accountModel);

    void deleteAccount(AccountModel accountModel);

    void createUpdateCard(CardModel cardModel);

    void deleteCard(CardModel cardModel);
}
