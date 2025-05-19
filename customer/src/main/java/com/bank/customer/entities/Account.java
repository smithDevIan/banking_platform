package com.bank.customer.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
public class Account {
    @Id
    private UUID id;
    private String iban;
    private String bicSwift;
    @ManyToOne
    private Customer customer;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastUpdated;
    private LocalDateTime dateDeleted;

    public Account(UUID id) {
        this.id = id;
    }
}
