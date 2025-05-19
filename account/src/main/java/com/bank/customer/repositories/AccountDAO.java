package com.bank.customer.repositories;

import com.bank.customer.entities.Account;
import com.bank.customer.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccountDAO extends JpaRepository<Account, UUID> {
    Account findFirstByIdAndDateDeletedIsNull(UUID id);
    @Query("""
        SELECT a FROM Account a
        LEFT JOIN Card c ON c.account.id = a.id
        WHERE a.dateDeleted IS NULL
        AND (:iban IS NULL OR :iban = '' OR LOWER(a.iban) LIKE LOWER(CONCAT('%', CAST(:iban AS string), '%')))
        AND (:bicSwift IS NULL OR :bicSwift = '' OR LOWER(a.bicSwift) LIKE LOWER(CONCAT('%', CAST(:bicSwift AS string), '%')))
        AND (:cardAlias IS NULL OR :cardAlias = '' OR
             (c IS NOT NULL AND LOWER(c.cardAlias) LIKE LOWER(CONCAT('%', CAST(:cardAlias AS string), '%'))))
        """)
    Page<Account> findAllByIbanAndBicSwiftAndCardAliasAndDateDeletedIsNull(@Param("iban") String iban,@Param("bicSwift") String bicSwift,@Param("cardAlias") String cardAlias, Pageable pageable);

    List<Account> findAllByCustomerAndDateDeletedIsNull(Customer customer);
}
