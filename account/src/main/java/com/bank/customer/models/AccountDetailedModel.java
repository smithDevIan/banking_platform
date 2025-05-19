package com.bank.customer.models;

import com.bank.customer.entities.Account;
import com.bank.customer.utils.ItemId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailedModel {
    private UUID id;
    private String iban;
    private String bicSwift;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastUpdated;
    private LocalDateTime dateDeleted;
    private ItemId customer;
    private List<CardShortModel> cards;

    public AccountDetailedModel(Account account,List<CardShortModel> cardShortModels){
        id = account.getId();
        iban = account.getIban();
        bicSwift = account.getBicSwift();
        customer = new ItemId(
                account.getCustomer().getId(),
                String.format(
                        "%s %s%s",
                        account.getCustomer().getFirstName(),
                        account.getCustomer().getLastName(),
                        account.getCustomer().getOtherName() != null ? " " + account.getCustomer().getOtherName() : ""
                )
        );
        dateCreated = account.getDateCreated();
        dateLastUpdated = account.getDateLastUpdated();
        dateDeleted = account.getDateDeleted();
        cards = cardShortModels;
    }
}
