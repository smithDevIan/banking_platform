package com.bank.card.repositories;

import com.bank.card.entities.Account;
import com.bank.card.entities.Card;
import com.bank.card.entities.CardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CardDAO extends JpaRepository<Card, UUID> {
    Card findFirstByIdAndDateDeletedIsNull(UUID id);

    Card findFirstByCardTypeAndAccountAndDateDeletedIsNull(CardType cardType, Account account);
    @Query("""
        SELECT c FROM Card c
        WHERE c.dateDeleted IS NULL
        AND (:cardType IS NULL OR :cardType = '' OR LOWER(c.cardType.code) LIKE LOWER(CONCAT('%', CAST(:cardType AS string), '%')))
        AND (:pan IS NULL OR :pan = '' OR LOWER(c.pan) LIKE LOWER(CONCAT('%', CAST(:pan AS string), '%')))
        AND (:cardAlias IS NULL OR :cardAlias = '' OR
             (c IS NOT NULL AND LOWER(c.cardAlias) LIKE LOWER(CONCAT('%', CAST(:cardAlias AS string), '%'))))
        """)
    Page<Card> findAllByCardTypeAndPanAndCardAliasAndDateDeletedIsNull(@Param("cardType") String cardType,@Param("pan") String pan,@Param("cardAlias") String alias, Pageable pageable);

    List<Card> findAllByAccountAndDateDeletedIsNull(Account account);
}
