package com.bank.card.repositories;

import com.bank.card.entities.CardType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTypeDAO extends JpaRepository<CardType,String> {
    CardType findByCode(String cardType);
}
