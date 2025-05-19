package com.bank.customer.services.interfaces;

import com.bank.customer.kafka.models.CardModel;
import com.bank.customer.models.CustomerModel;

public interface KafkaService {
    void createUpdateCustomer(CustomerModel customerModel);

    void deleteCustomer(CustomerModel customerModel);

    void createUpdateCard(CardModel cardModel);

    void deleteCard(CardModel cardModel);
}
