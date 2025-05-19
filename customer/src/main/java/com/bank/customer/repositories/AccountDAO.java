package com.bank.customer.repositories;

import com.bank.customer.entities.Account;
import com.bank.customer.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountDAO extends JpaRepository<Account, UUID> {
    Account findFirstByIdAndDateDeletedIsNull(UUID id);

    List<Account> findAllByCustomerAndDateDeletedIsNullOrderByDateCreatedDesc(Customer customer);
}
