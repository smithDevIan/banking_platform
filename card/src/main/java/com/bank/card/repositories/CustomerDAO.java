package com.bank.card.repositories;

import com.bank.card.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerDAO extends JpaRepository<Customer, UUID> {
    Customer findFirstByIdAndDateDeletedIsNull(UUID id);
}
