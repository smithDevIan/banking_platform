package com.bank.customer.services.interfaces;

import com.bank.customer.entities.Customer;
import com.bank.customer.models.AccountDetailedModel;
import com.bank.customer.models.AccountModel;
import com.bank.customer.payloads.AccountCreateRequest;
import com.bank.customer.utils.ResponseData;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AccountService {
    ResponseData<Object> createUpdate(AccountCreateRequest accountCreateRequest, UUID id);

    Object getAll(Pageable pageable, String iban, String bicSwift, String cardAlias);

    ResponseData<AccountDetailedModel> getOne(UUID id);

    ResponseData<UUID> delete(UUID id);

    void deleteCustomerAccounts(Customer customer);
}
