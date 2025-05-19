package com.bank.card.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDataModel {
    private UUID id;
    private String firstName;
    private String lastName;
    private String otherName;
    private LocalDate dateCreated;
    private LocalDate dateLastUpdated;
    private LocalDate dateDeleted;
}
