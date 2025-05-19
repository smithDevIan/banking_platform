package com.bank.customer.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
public class Card {
    @Id
    private UUID id;
    private String cardAlias;
    @ManyToOne
    private CardType cardType;
    @ManyToOne
    private Account account;
    private String pan;
    private String cvv;
    private LocalDateTime dateCreated;
    private LocalDateTime dateLastUpdated;
    private LocalDateTime dateDeleted;

    public Card(UUID id) {
        this.id = id;
    }
}
