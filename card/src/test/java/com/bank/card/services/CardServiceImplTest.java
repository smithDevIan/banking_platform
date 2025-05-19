package com.bank.card.services;

import com.bank.card.entities.*;
import com.bank.card.kafka.MessageProducer;
import com.bank.card.models.CardModel;
import com.bank.card.models.ValidationResult;
import com.bank.card.payloads.CardCreateRequest;
import com.bank.card.repositories.AccountDAO;
import com.bank.card.repositories.CardDAO;
import com.bank.card.repositories.CardTypeDAO;
import com.bank.card.utils.ResponseData;
import com.bank.card.utils.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceImplTest {

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private AccountDAO accountDAO;

    @Mock
    private CardDAO cardDAO;

    @Mock
    private CardTypeDAO cardTypeDAO;

    @Mock
    private MessageProducer messageProducer;

    private UUID cardId;
    private Account account;
    private CardType cardType;
    private Card card;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardId = UUID.randomUUID();

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());

        account = new Account();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);

        cardType = new CardType();
        cardType.setCode("virtual");
        cardType.setName("Visa");

        card = new Card();
        card.setId(cardId);
        card.setAccount(account);
        card.setCardType(cardType);
        card.setPan("1234567812345678");
        card.setCvv("123");
        card.setCardAlias("My Card");
    }

    @Test
    void testCreateUpdate_newCard_success() {
        CardCreateRequest request = new CardCreateRequest();
        request.setAccountId(account.getId());
        request.setCardAlias("Test Alias");
        request.setPan("4539578763621486");
        request.setCvv(123);
        request.setCardType("virtual");

        when(cardTypeDAO.findByCode("virtual")).thenReturn(cardType);
        when(accountDAO.findFirstByIdAndDateDeletedIsNull(request.getAccountId())).thenReturn(account);
        when(cardDAO.findFirstByCardTypeAndAccountAndDateDeletedIsNull(eq(cardType), any())).thenReturn(null);

        ResponseData<Object> response = cardService.createUpdate(request, null);

        assertEquals(Response.SUCCESS.status().getCode(), response.getStatus().getCode());
        verify(cardDAO, times(1)).save(any(Card.class));
        verify(messageProducer, times(1)).publish(any(), any());

    }

    @Test
    void testGetOne_found() {
        when(cardDAO.findFirstByIdAndDateDeletedIsNull(cardId)).thenReturn(card);

        ResponseData<CardModel> response = cardService.getOne(cardId, true);

        assertNotNull(response.getData());
        assertEquals(Response.SUCCESS.status().getCode(), response.getStatus().getCode());
    }

    @Test
    void testGetOne_notFound() {
        when(cardDAO.findFirstByIdAndDateDeletedIsNull(cardId)).thenReturn(null);

        ResponseData<CardModel> response = cardService.getOne(cardId, true);

        assertNull(response.getData());
        assertEquals(Response.CARD_NOT_FOUND.status().getCode(), response.getStatus().getCode());
    }

    @Test
    void testDelete_cardExists() {
        when(cardDAO.findFirstByIdAndDateDeletedIsNull(cardId)).thenReturn(card);

        ResponseData<UUID> response = cardService.delete(cardId);

        assertEquals(Response.CARD_DELETED.status().getCode(), response.getStatus().getCode());
        verify(cardDAO, times(1)).save(any(Card.class));
        verify(messageProducer, times(1)).publish(any(), any());
    }

    @Test
    void testDelete_cardNotFound() {
        when(cardDAO.findFirstByIdAndDateDeletedIsNull(cardId)).thenReturn(null);

        ResponseData<UUID> response = cardService.delete(cardId);

        assertEquals(Response.CARD_NOT_FOUND.status().getCode(), response.getStatus().getCode());
        verify(cardDAO, never()).save(any());
    }

    @Test
    void testGetAll_returnsCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(List.of(card));

        when(cardDAO.findAllByCardTypeAndPanAndCardAliasAndDateDeletedIsNull(anyString(), anyString(), anyString(), eq(pageable)))
                .thenReturn(page);

        Object response = cardService.getAll(pageable, "virtual", "1234", "Alias", true);

        assertNotNull(response);
    }

    @Test
    void testDeleteAllAccountCards() {
        List<Card> cards = List.of(card);
        when(cardDAO.findAllByAccountAndDateDeletedIsNull(account)).thenReturn(cards);
        when(cardDAO.findFirstByIdAndDateDeletedIsNull(cardId)).thenReturn(card);

        card.setId(cardId);
        cardService.deleteAllAccountCards(account);

        verify(cardDAO, times(1)).save(any());
    }
}
