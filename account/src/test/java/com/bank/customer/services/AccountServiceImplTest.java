package com.bank.customer.services;

import com.bank.customer.entities.*;
import com.bank.customer.kafka.MessageProducer;
import com.bank.customer.models.*;
import com.bank.customer.payloads.AccountCreateRequest;
import com.bank.customer.repositories.*;
import com.bank.customer.utils.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock private AccountDAO accountDAO;
    @Mock private CustomerDAO customerDAO;
    @Mock private CardDAO cardDAO;
    @Mock private MessageProducer messageProducer;

    @InjectMocks private AccountServiceImpl accountService;

    private UUID customerId = UUID.randomUUID();
    private UUID accountId = UUID.randomUUID();
    private AccountCreateRequest validRequest;
    private AccountCreateRequest invalidRequest;
    private Customer customer;
    private Account existingAccount;

    @BeforeEach
    void setup() {
        // Setup test data
        validRequest = new AccountCreateRequest();
        validRequest.setCustomerId(customerId);
        validRequest.setIban("GB29NWBK60161331926819"); // Valid IBAN
        validRequest.setBicSwift("BARCGB22"); // Valid BIC

        invalidRequest = new AccountCreateRequest();
        invalidRequest.setCustomerId(customerId);
        invalidRequest.setIban("invalid-iban");
        invalidRequest.setBicSwift("invalid-bic");

        customer = new Customer();
        customer.setId(customerId);

        existingAccount = new Account();
        existingAccount.setId(accountId);
        existingAccount.setCustomer(customer);
        existingAccount.setIban(validRequest.getIban());
        existingAccount.setBicSwift(validRequest.getBicSwift());
        existingAccount.setDateCreated(LocalDateTime.now().minusDays(1));
    }

    // Test helper method
    private void setupAccountTopic() {
        ReflectionTestUtils.setField(accountService, "accountTopic", "account-topic");
    }

    // --- createUpdate Tests ---
    @Test
    void createAccount_withValidRequest_shouldCreateSuccessfully() {
        setupAccountTopic();
        when(customerDAO.findFirstByIdAndDateDeletedIsNull(customerId)).thenReturn(customer);
        when(accountDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseData<Object> response = accountService.createUpdate(validRequest, null);

        assertSuccessResponse(response, AccountModel.class);
        verify(accountDAO).save(any());
        verifyKafkaMessage(Constants.Kafka.CREATE_UPDATE);
    }

    @Test
    void updateAccount_withValidRequest_shouldUpdateSuccessfully() {
        setupAccountTopic();
        when(customerDAO.findFirstByIdAndDateDeletedIsNull(customerId)).thenReturn(customer);
        when(accountDAO.findFirstByIdAndDateDeletedIsNull(accountId)).thenReturn(existingAccount);
        when(accountDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseData<Object> response = accountService.createUpdate(validRequest, accountId);

        assertSuccessResponse(response, AccountModel.class);
        verify(accountDAO).save(any());
        verifyKafkaMessage(Constants.Kafka.CREATE_UPDATE);
    }

    @Test
    void createAccount_withInvalidData_shouldReturnErrors() {
        ResponseData<Object> response = accountService.createUpdate(invalidRequest, null);

        assertErrorResponse(response, 3); // Expecting 3 validation errors
    }

    @Test
    void updateAccount_withNonExistingAccount_shouldReturnError() {
        when(customerDAO.findFirstByIdAndDateDeletedIsNull(customerId)).thenReturn(customer);
        when(accountDAO.findFirstByIdAndDateDeletedIsNull(accountId)).thenReturn(null);

        ResponseData<Object> response = accountService.createUpdate(validRequest, accountId);

        assertErrorResponse(response, 1);
        assertEquals("id", ((List<FieldError>)response.getData()).get(0).getField());
    }

    // --- getAll Tests ---
    @Test
    void getAllAccounts_shouldReturnPagedResults() {
        Pageable pageable = Pageable.unpaged();
        Page<Account> page = new PageImpl<>(List.of(existingAccount));

        when(accountDAO.findAllByIbanAndBicSwiftAndCardAliasAndDateDeletedIsNull(
                any(), any(), any(), eq(pageable))).thenReturn(page);

        Object result = accountService.getAll(pageable, null, null, null);

        assertPagedResponse(result, 1);
    }

    @Test
    void getAllAccounts_withFilters_shouldApplyFilters() {
        Pageable pageable = Pageable.unpaged();
        Page<Account> page = new PageImpl<>(List.of(existingAccount));

        when(accountDAO.findAllByIbanAndBicSwiftAndCardAliasAndDateDeletedIsNull(
                eq("filter"), eq("filter"), eq("filter"), eq(pageable))).thenReturn(page);

        Object result = accountService.getAll(pageable, "filter", "filter", "filter");

        assertPagedResponse(result, 1);
    }

    // --- getOne Tests ---
    @Test
    void getAccount_withCards_shouldReturnDetailedModel() {
        when(accountDAO.findFirstByIdAndDateDeletedIsNull(accountId)).thenReturn(existingAccount);

        Card card = createTestCard();
        when(cardDAO.findAllByAccountAndDateDeletedIsNullOrderByDateCreatedDesc(existingAccount))
                .thenReturn(List.of(card));

        ResponseData<AccountDetailedModel> response = accountService.getOne(accountId);

        assertSuccessResponse(response, AccountDetailedModel.class);
        assertEquals(1, response.getData().getCards().size());
    }

    @Test
    void getAccount_notFound_shouldReturnError() {
        when(accountDAO.findFirstByIdAndDateDeletedIsNull(accountId)).thenReturn(null);

        ResponseData<AccountDetailedModel> response = accountService.getOne(accountId);

        assertEquals(Response.ACCOUNT_NOT_FOUND.status().getCode(), response.getStatus().getCode());
    }

    // --- delete Tests ---
    @Test
    void deleteAccount_shouldSoftDelete() {
        setupAccountTopic();
        when(accountDAO.findFirstByIdAndDateDeletedIsNull(accountId)).thenReturn(existingAccount);
        when(accountDAO.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseData<UUID> response = accountService.delete(accountId);

        assertEquals(Response.ACCOUNT_DELETED.status().getCode(), response.getStatus().getCode());
        assertNotNull(existingAccount.getDateDeleted());
        verifyKafkaMessage(Constants.Kafka.DELETE);
    }

    @Test
    void deleteAccount_notFound_shouldReturnError() {
        when(accountDAO.findFirstByIdAndDateDeletedIsNull(accountId)).thenReturn(null);

        ResponseData<UUID> response = accountService.delete(accountId);

        assertEquals(Response.ACCOUNT_NOT_FOUND.status().getCode(), response.getStatus().getCode());
        verify(accountDAO, never()).save(any());
    }

    // --- Helper Methods ---
    private Card createTestCard() {
        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setAccount(existingAccount);
        card.setCardType(new CardType());
        return card;
    }

    private void assertSuccessResponse(ResponseData<?> response, Class<?> expectedType) {
        assertEquals(Response.SUCCESS.status().getCode(), response.getStatus().getCode());
        assertTrue(expectedType.isInstance(response.getData()));
    }

    private void assertErrorResponse(ResponseData<?> response, int expectedErrorCount) {
        assertEquals(Response.ERRORS_OCCURRED.status().getCode(), response.getStatus().getCode());
        assertEquals(expectedErrorCount, ((List<?>)response.getData()).size());
    }

    private void assertPagedResponse(Object result, int expectedSize) {
        assertTrue(result instanceof PagedResponse);
        PagedResponse<?> response = (PagedResponse<?>) result;
        assertEquals(expectedSize, response.getData().size());
        assertEquals(Response.SUCCESS.status().getCode(), response.getStatus().getCode());
    }

    private void verifyKafkaMessage(String action) {
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageProducer).publish(topicCaptor.capture(), messageCaptor.capture());

        assertEquals("account-topic", topicCaptor.getValue());
        assertTrue(messageCaptor.getValue().contains(action));
    }
}