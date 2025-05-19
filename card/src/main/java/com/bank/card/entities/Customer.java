package com.bank.card.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
public class Customer {
    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String otherName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastUpdated;
    private LocalDateTime dateDeleted;
    public Customer(UUID id) {
        this.id = id;
    }
}
