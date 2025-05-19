package com.bank.card.repositories;

import com.bank.card.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AccountDAO extends JpaRepository<Account, UUID> {
    Account findFirstByIdAndDateDeletedIsNull(UUID id);
}
