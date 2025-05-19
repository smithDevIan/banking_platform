package com.bank.customer.kafka.models;

import com.bank.customer.entities.Account;
import com.bank.customer.utils.ItemId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountModel {
    private UUID id;
    private String iban;
    private String bicSwift;
    private ItemId customer;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastUpdated;
    private LocalDateTime dateDeleted;

    public AccountModel(Account account){
        id = account.getId();
        iban = account.getIban();
        bicSwift = account.getBicSwift();
        customer = new ItemId(account.getCustomer().getId(),String.format("%s %s %s",account.getCustomer().getFirstName(),account.getCustomer().getLastName(),account.getCustomer().getOtherName()));
        dateCreated = account.getDateCreated();
        dateLastUpdated = account.getDateLastUpdated();
        dateDeleted = account.getDateDeleted();
    }
}
