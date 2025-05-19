package com.bank.customer.repositories;

import com.bank.customer.entities.Account;
import com.bank.customer.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardDAO extends JpaRepository<Card, UUID> {
    Card findFirstByIdAndDateDeletedIsNull(UUID id);

    List<Card> findAllByAccountAndDateDeletedIsNullOrderByDateCreatedDesc(Account account);
}
