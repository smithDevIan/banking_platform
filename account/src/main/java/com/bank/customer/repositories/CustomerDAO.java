package com.bank.customer.repositories;

import com.bank.customer.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface CustomerDAO extends JpaRepository<Customer, UUID> {
    Customer findFirstByIdAndDateDeletedIsNull(UUID id);
}
