package com.bank.card.services.interfaces;

import com.bank.card.models.AccountModel;
import com.bank.card.models.CustomerModel;

public interface KafkaService {
    void createUpdateCustomer(CustomerModel customerModel);

    void deleteCustomer(CustomerModel customerModel);

    void createUpdateAccount(AccountModel accountModel);

    void deleteAccount(AccountModel accountModel);
}
