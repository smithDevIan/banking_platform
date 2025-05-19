package com.bank.customer.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
