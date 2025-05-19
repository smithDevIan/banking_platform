package com.bank.customer.services;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bank.customer.entities.*;
import com.bank.customer.kafka.models.*;
import com.bank.customer.models.CustomerModel;
import com.bank.customer.repositories.AccountDAO;
import com.bank.customer.repositories.CardDAO;
import com.bank.customer.utils.ItemCode;
import com.bank.customer.utils.ItemId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class KafkaServiceImplTest {

    @Mock
    private AccountDAO accountDAO;

    @Mock
    private CardDAO cardDAO;

    @InjectMocks
    private KafkaServiceImpl kafkaService;

    private final UUID testAccountId = UUID.randomUUID();
    private final UUID testCustomerId = UUID.randomUUID();
    private final UUID testCardId = UUID.randomUUID();
    private final LocalDateTime testTimestamp = LocalDateTime.now();

    @Test
    void createUpdateAccount_WhenAccountDoesNotExist_ShouldCreateNewAccount() {
        AccountModel accountModel = new AccountModel();
        accountModel.setId(testAccountId);
        accountModel.setIban("TESTIBAN123");
        accountModel.setBicSwift("TESTBIC");
        accountModel.setDateCreated(testTimestamp);

        CustomerModel customerModel = new CustomerModel();
        customerModel.setId(testCustomerId);
        accountModel.setCustomer(new ItemId(customerModel.getId(),String.format("%s %s",customerModel.getFirstName(),customerModel.getLastName())));

        when(accountDAO.findFirstByIdAndDateDeletedIsNull(testAccountId)).thenReturn(null);

        kafkaService.createUpdateAccount(accountModel);

        verify(accountDAO).save(any(Account.class));
        verify(accountDAO).findFirstByIdAndDateDeletedIsNull(testAccountId);
    }

    @Test
    void createUpdateAccount_WhenAccountExists_ShouldUpdateAccount() {
        // Arrange
        AccountModel accountModel = new AccountModel();
        accountModel.setId(testAccountId);
        accountModel.setIban("UPDATEDIBAN");
        accountModel.setBicSwift("UPDATEDBIC");
        accountModel.setDateLastUpdated(testTimestamp);

        CustomerModel customerModel = new CustomerModel();
        customerModel.setId(testCustomerId);
        accountModel.setCustomer(new ItemId(customerModel.getId(),String.format("%s %s",customerModel.getFirstName(),customerModel.getLastName())));

        Account existingAccount = new Account();
        existingAccount.setId(testAccountId);

        when(accountDAO.findFirstByIdAndDateDeletedIsNull(testAccountId)).thenReturn(existingAccount);

        kafkaService.createUpdateAccount(accountModel);

        // Assert
        verify(accountDAO).save(existingAccount);
        assertEquals("UPDATEDIBAN", existingAccount.getIban());
        assertEquals("UPDATEDBIC", existingAccount.getBicSwift());
        assertEquals(testTimestamp, existingAccount.getDateLastUpdated());
    }

    @Test
    void deleteAccount_WhenAccountExists_ShouldSoftDelete() {
        // Arrange
        AccountModel accountModel = new AccountModel();
        accountModel.setId(testAccountId);
        accountModel.setDateDeleted(testTimestamp);

        Account existingAccount = new Account();
        existingAccount.setId(testAccountId);

        when(accountDAO.findFirstByIdAndDateDeletedIsNull(testAccountId)).thenReturn(existingAccount);

        // Act
        kafkaService.deleteAccount(accountModel);

        // Assert
        verify(accountDAO).save(existingAccount);
        assertEquals(testTimestamp, existingAccount.getDateDeleted());
    }

    @Test
    void deleteAccount_WhenAccountDoesNotExist_ShouldDoNothing() {
        // Arrange
        AccountModel accountModel = new AccountModel();
        accountModel.setId(testAccountId);
        accountModel.setDateDeleted(testTimestamp);

        when(accountDAO.findFirstByIdAndDateDeletedIsNull(testAccountId)).thenReturn(null);

        // Act
        kafkaService.deleteAccount(accountModel);

        // Assert
        verify(accountDAO).findFirstByIdAndDateDeletedIsNull(testAccountId);
        verify(accountDAO, never()).save(any());
    }

    @Test
    void createUpdateCard_WhenCardDoesNotExist_ShouldCreateNewCard() {
        // Arrange
        CardModel cardModel = new CardModel();
        cardModel.setId(testCardId);
        cardModel.setDateCreated(testTimestamp);
        cardModel.setCardAlias("My Card");
        cardModel.setPan("4111111111111111");
        cardModel.setCvv("123");

        ItemCode cardTypeModel = new ItemCode();
        cardTypeModel.setCode("virtual");
        cardModel.setCardType(cardTypeModel);

        AccountModel accountModel = new AccountModel();
        accountModel.setId(testAccountId);
        cardModel.setAccount(accountModel);

        when(cardDAO.findFirstByIdAndDateDeletedIsNull(testCardId)).thenReturn(null);

        // Act
        kafkaService.createUpdateCard(cardModel);

        // Assert
        verify(cardDAO).save(any(Card.class));
        verify(cardDAO).findFirstByIdAndDateDeletedIsNull(testCardId);
    }

    @Test
    void createUpdateCard_WhenCardExists_ShouldUpdateCard() {
        // Arrange
        CardModel cardModel = new CardModel();
        cardModel.setId(testCardId);
        cardModel.setDateLastUpdated(testTimestamp);
        cardModel.setCardAlias("Updated Alias");
        cardModel.setPan("5555555555554444");
        cardModel.setCvv("321");

        ItemCode cardTypeModel = new ItemCode();
        cardTypeModel.setCode("virtual");
        cardModel.setCardType(cardTypeModel);

        AccountModel accountModel = new AccountModel();
        accountModel.setId(testAccountId);
        cardModel.setAccount(accountModel);

        Card existingCard = new Card();
        existingCard.setId(testCardId);

        when(cardDAO.findFirstByIdAndDateDeletedIsNull(testCardId)).thenReturn(existingCard);

        // Act
        kafkaService.createUpdateCard(cardModel);

        // Assert
        verify(cardDAO).save(existingCard);
        assertEquals("Updated Alias", existingCard.getCardAlias());
        assertEquals("5555555555554444", existingCard.getPan());
        assertEquals("***", existingCard.getCvv());
        assertEquals("virtual", existingCard.getCardType().getCode());
        assertEquals(testAccountId, existingCard.getAccount().getId());
        assertEquals(testTimestamp, existingCard.getDateLastUpdated());
    }

    @Test
    void deleteCard_WhenCardExists_ShouldSoftDelete() {
        // Arrange
        CardModel cardModel = new CardModel();
        cardModel.setId(testCardId);
        cardModel.setDateDeleted(testTimestamp);

        Card existingCard = new Card();
        existingCard.setId(testCardId);

        when(cardDAO.findFirstByIdAndDateDeletedIsNull(testCardId)).thenReturn(existingCard);

        // Act
        kafkaService.deleteCard(cardModel);

        // Assert
        verify(cardDAO).save(existingCard);
        assertEquals(testTimestamp, existingCard.getDateDeleted());
    }

    @Test
    void deleteCard_WhenCardDoesNotExist_ShouldDoNothing() {
        // Arrange
        CardModel cardModel = new CardModel();
        cardModel.setId(testCardId);
        cardModel.setDateDeleted(testTimestamp);

        when(cardDAO.findFirstByIdAndDateDeletedIsNull(testCardId)).thenReturn(null);

        // Act
        kafkaService.deleteCard(cardModel);

        // Assert
        verify(cardDAO).findFirstByIdAndDateDeletedIsNull(testCardId);
        verify(cardDAO, never()).save(any());
    }

    @Test
    void createUpdateAccount_WhenCustomerIsNull_ShouldHandleGracefully() {

        // Arrange
        AccountModel accountModel = new AccountModel();
        accountModel.setId(testAccountId);
        accountModel.setIban("TESTIBAN123");
        accountModel.setBicSwift("TESTBIC");
        accountModel.setDateCreated(testTimestamp);
        ItemId customer = new ItemId();
        accountModel.setCustomer(customer); // Explicit null

        when(accountDAO.findFirstByIdAndDateDeletedIsNull(testAccountId)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> kafkaService.createUpdateAccount(accountModel));
        verify(accountDAO).save(any(Account.class));
    }

    @Test
    void createUpdateCard_WhenCardTypeIsNull_ShouldHandleGracefully() {
        // Arrange
        CardModel cardModel = new CardModel();
        cardModel.setId(testCardId);
        cardModel.setDateCreated(testTimestamp);
        cardModel.setCardAlias("My Card");
        cardModel.setPan("4111111111111111");
        cardModel.setCvv("123");

        ItemCode cardTypeModel = new ItemCode();
        cardModel.setCardType(cardTypeModel);

        AccountModel accountModel = new AccountModel();
        accountModel.setId(testAccountId);
        cardModel.setAccount(accountModel);

        when(cardDAO.findFirstByIdAndDateDeletedIsNull(testCardId)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> kafkaService.createUpdateCard(cardModel));
        verify(cardDAO).save(any(Card.class));
    }
}