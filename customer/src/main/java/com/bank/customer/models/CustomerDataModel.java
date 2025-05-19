package com.bank.customer.models;

import com.bank.customer.entities.Customer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDataModel {
    private UUID id;
    private String firstName;
    private String lastName;
    private String otherName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastUpdated;
    private LocalDateTime dateDeleted;
    private List<AccountDetailedModel> accounts;

    public CustomerDataModel(Customer customer,List<AccountDetailedModel> accountDetailedModelList){
        id = customer.getId();
        firstName = customer.getFirstName();
        lastName = customer.getLastName();
        otherName = customer.getOtherName();
        dateCreated = customer.getDateCreated();
        dateLastUpdated = customer.getDateLastUpdated();
        dateDeleted = customer.getDateDeleted();
        accounts = accountDetailedModelList;
    }
}
