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
    @Query("""
    SELECT c FROM Customer c
    WHERE c.dateDeleted IS NULL
    AND (
        CAST(:name AS string) IS NULL OR :name = '' OR
        LOWER(c.firstName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')) OR
        LOWER(c.lastName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')) OR
        LOWER(c.otherName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')) OR
        LOWER(CONCAT(c.firstName, '', COALESCE(c.otherName, ''), '', c.lastName))
            LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))
    )
    AND (
        (CAST(:startDate AS date) IS NULL OR c.dateCreated >= CAST(:startDate AS date))
        AND (CAST(:endDate AS date) IS NULL OR c.dateCreated < CAST(:endDate AS date))
    )
""")
    Page<Customer> findAllByNameOrDateRangeWhereDateDeletedIsNull(@Param("name") String name, @Param("startDate") LocalDateTime start, @Param("endDate") LocalDateTime end, Pageable pageable);
}
