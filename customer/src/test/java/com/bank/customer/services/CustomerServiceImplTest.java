package com.bank.customer.services;

import com.bank.customer.entities.*;
import com.bank.customer.kafka.MessageProducer;
import com.bank.customer.models.*;
import com.bank.customer.payloads.CustomerCreateRequest;
import com.bank.customer.repositories.*;
import com.bank.customer.utils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerDAO customerDAO;

    @Mock
    private CardDAO cardDAO;

    @Mock
    private AccountDAO accountDAO;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerCreateRequest validRequest;
    private Customer existingCustomer;
    private UUID existingCustomerId;
    private UUID nonExistingCustomerId;

    @BeforeEach
    void setUp() {
        existingCustomerId = UUID.randomUUID();
        nonExistingCustomerId = UUID.randomUUID();

        validRequest = new CustomerCreateRequest("John", "Doe", "Middle");

        existingCustomer = new Customer();
        existingCustomer.setId(existingCustomerId);
        existingCustomer.setFirstName("Existing");
        existingCustomer.setLastName("Customer");
        existingCustomer.setDateCreated(LocalDateTime.now().minusDays(1));
    }

    @Test
    void createUpdate_WhenCreatingNewCustomer_ShouldCreateAndReturnSuccess() {
        // Arrange
        when(customerDAO.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        // Act
        ResponseData<?> response = customerService.createUpdate(validRequest, null);

        // Assert
        assertEquals(Response.SUCCESS.status().getCode(), response.getStatus().getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof CustomerModel);

        verify(customerDAO).save(any(Customer.class));
        verify(messageProducer).publish(any(), anyString());
    }

    @Test
    void createUpdate_WhenUpdatingExistingCustomer_ShouldUpdateAndReturnSuccess() {
        when(customerDAO.findFirstByIdAndDateDeletedIsNull(existingCustomerId)).thenReturn(existingCustomer);
        when(customerDAO.save(any(Customer.class))).thenReturn(existingCustomer);

        ResponseData<?> response = customerService.createUpdate(validRequest, existingCustomerId);

        assertEquals(Response.SUCCESS.status().getCode(), response.getStatus().getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof CustomerModel);

        verify(customerDAO).save(any(Customer.class));
        verify(messageProducer).publish(any(), anyString());
    }

    @Test
    void createUpdate_WhenCustomerNotFound_ShouldReturnNotFound() {
        when(customerDAO.findFirstByIdAndDateDeletedIsNull(nonExistingCustomerId)).thenReturn(null);

        ResponseData<?> response = (ResponseData<?>) customerService.createUpdate(validRequest, nonExistingCustomerId);

        assertEquals(Response.CUSTOMER_NOT_FOUND.status().getCode(), response.getStatus().getCode());
        assertNull(response.getData());

        verify(customerDAO).findFirstByIdAndDateDeletedIsNull(nonExistingCustomerId);
        verify(customerDAO, never()).save(any());
        verify(messageProducer, never()).publish(anyString(), anyString());
    }

    @Test
    void getCustomers_ShouldReturnPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10); // First page (0), size=10
        Page<Customer> customerPage = new PageImpl<>(
                List.of(existingCustomer),
                pageable,
                1L
        );

        when(customerDAO.findAllByNameOrDateRangeWhereDateDeletedIsNull(anyString(), any(), any(), eq(pageable)))
                .thenReturn(customerPage);

        PagedResponse<?> response = (PagedResponse<?>) customerService.getCustomers(pageable, "John", null, null);

        assertEquals(Response.SUCCESS.status().getCode(), response.getStatus().getCode());
        assertEquals(1, response.getData().size()); // 1 customer in current page
        assertEquals(1, response.getPage()); // Assuming 1-based page numbering
        assertEquals(10, response.getSize()); // Requested page size
        assertEquals(1, response.getTotalElements()); // Total elements in DB

        verify(customerDAO).findAllByNameOrDateRangeWhereDateDeletedIsNull(anyString(), any(), any(), eq(pageable));
    }

    @Test
    void getOne_WhenCustomerExists_ShouldReturnCustomerWithDetails() {
        // Arrange
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setCustomer(existingCustomer);

        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setAccount(account);

        // Add this - create and set a CardType
        CardType cardType = new CardType();
        cardType.setName("virtual");
        card.setCardType(cardType);

        when(customerDAO.findFirstByIdAndDateDeletedIsNull(existingCustomerId)).thenReturn(existingCustomer);
        when(accountDAO.findAllByCustomerAndDateDeletedIsNullOrderByDateCreatedDesc(existingCustomer))
                .thenReturn(List.of(account));
        when(cardDAO.findAllByAccountAndDateDeletedIsNullOrderByDateCreatedDesc(account))
                .thenReturn(List.of(card));

        // Act
        ResponseData<?> response = (ResponseData<?>) customerService.getOne(existingCustomerId);

        // Assert
        assertEquals(Response.SUCCESS.status().getCode(), response.getStatus().getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof CustomerDataModel);

        CustomerDataModel model = (CustomerDataModel) response.getData();
        assertEquals(1, model.getAccounts().size());
        assertEquals(1, model.getAccounts().get(0).getCards().size());

        verify(customerDAO).findFirstByIdAndDateDeletedIsNull(existingCustomerId);
        verify(accountDAO).findAllByCustomerAndDateDeletedIsNullOrderByDateCreatedDesc(existingCustomer);
        verify(cardDAO).findAllByAccountAndDateDeletedIsNullOrderByDateCreatedDesc(account);
    }

    @Test
    void getOne_WhenCustomerNotFound_ShouldReturnNotFound() {
        when(customerDAO.findFirstByIdAndDateDeletedIsNull(nonExistingCustomerId)).thenReturn(null);

        ResponseData<?> response = (ResponseData<?>) customerService.getOne(nonExistingCustomerId);

        assertEquals(Response.CUSTOMER_NOT_FOUND.status().getCode(), response.getStatus().getCode());
        assertNull(response.getData());

        verify(customerDAO).findFirstByIdAndDateDeletedIsNull(nonExistingCustomerId);
        verify(accountDAO, never()).findAllByCustomerAndDateDeletedIsNullOrderByDateCreatedDesc(any());
    }

    @Test
    void deleteCustomer_WhenCustomerExists_ShouldSoftDeleteAndReturnSuccess() {
        when(customerDAO.findFirstByIdAndDateDeletedIsNull(existingCustomerId))
                .thenReturn(existingCustomer);
        when(customerDAO.save(existingCustomer))
                .thenReturn(existingCustomer);

        ResponseData<?> response = customerService.deleteCustomer(existingCustomerId);

        assertEquals(Response.CUSTOMER_DELETED.status().getCode(), response.getStatus().getCode());
        assertEquals(existingCustomerId, response.getData());
        assertNotNull(existingCustomer.getDateDeleted());

        verify(customerDAO).findFirstByIdAndDateDeletedIsNull(existingCustomerId);
        verify(customerDAO).save(existingCustomer);

        verify(messageProducer).publish(isNull(), anyString());
    }

    @Test
    void deleteCustomer_WhenCustomerNotFound_ShouldReturnNotFound() {
        when(customerDAO.findFirstByIdAndDateDeletedIsNull(nonExistingCustomerId)).thenReturn(null);

        ResponseData<?> response = customerService.deleteCustomer(nonExistingCustomerId);

        assertEquals(Response.CUSTOMER_NOT_FOUND.status().getCode(), response.getStatus().getCode());
        assertEquals(nonExistingCustomerId, response.getData());

        verify(customerDAO).findFirstByIdAndDateDeletedIsNull(nonExistingCustomerId);
        verify(customerDAO, never()).save(any());
        verify(messageProducer, never()).publish(anyString(), anyString());
    }
}