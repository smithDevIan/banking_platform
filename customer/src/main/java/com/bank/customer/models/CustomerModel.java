package com.bank.customer.models;

import com.bank.customer.entities.Customer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerModel {
    private UUID id;
    private String firstName;
    private String lastName;
    private String otherName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastUpdated;
    private LocalDateTime dateDeleted;

    public CustomerModel(Customer customer){
        id = customer.getId();
        firstName = customer.getFirstName();
        lastName = customer.getLastName();
        otherName = customer.getOtherName();
        dateCreated = customer.getDateCreated();
        dateLastUpdated = customer.getDateLastUpdated();
        dateDeleted = customer.getDateDeleted();
    }
}
