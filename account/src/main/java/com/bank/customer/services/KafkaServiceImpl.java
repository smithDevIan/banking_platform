package com.bank.customer.services;

import com.bank.customer.entities.Account;
import com.bank.customer.entities.Card;
import com.bank.customer.entities.CardType;
import com.bank.customer.entities.Customer;
import com.bank.customer.kafka.models.CardModel;
import com.bank.customer.models.CustomerModel;
import com.bank.customer.repositories.CardDAO;
import com.bank.customer.repositories.CustomerDAO;
import com.bank.customer.services.interfaces.AccountService;
import com.bank.customer.services.interfaces.KafkaService;
import com.bank.customer.utils.Translator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KafkaServiceImpl implements KafkaService {
    private final CustomerDAO customerDAO;
    private final CardDAO cardDAO;
    private final AccountService accountService;

    public KafkaServiceImpl(CustomerDAO customerDAO, CardDAO cardDAO, AccountService accountService) {
        this.customerDAO = customerDAO;
        this.cardDAO = cardDAO;
        this.accountService = accountService;
    }

    @Override
    public void createUpdateCustomer(CustomerModel customerModel) {
        Customer customer = customerDAO.findFirstByIdAndDateDeletedIsNull(customerModel.getId());
        if(customer != null){
            customer.setDateLastUpdated(customerModel.getDateLastUpdated());
        }else{
            customer = new Customer();
            customer.setId(customerModel.getId());
            customer.setDateCreated(customerModel.getDateCreated());
        }
        customer.setFirstName(customerModel.getFirstName());
        customer.setLastName(customerModel.getLastName());
        customer.setOtherName(customerModel.getOtherName());
        customerDAO.save(customer);
    }

    @Override
    public void deleteCustomer(CustomerModel customerModel) {
        Customer customer = customerDAO.findFirstByIdAndDateDeletedIsNull(customerModel.getId());
        if(customer != null){
            customer.setFirstName(customerModel.getFirstName());
            customer.setLastName(customerModel.getLastName());
            customer.setOtherName(customerModel.getOtherName());
            customer.setDateDeleted(customerModel.getDateDeleted());
            customerDAO.save(customer);

            accountService.deleteCustomerAccounts(customer);
        }
    }

    @Override
    public void createUpdateCard(CardModel cardModel) {
        Card card = cardDAO.findFirstByIdAndDateDeletedIsNull(cardModel.getId());
        if (card != null) {
            card.setDateLastUpdated(cardModel.getDateLastUpdated());
        } else {
            card = new Card();
            card.setId(cardModel.getId());
            card.setDateCreated(cardModel.getDateCreated());
        }
        card.setCardType(new CardType(cardModel.getCardType().getCode()));
        card.setCardAlias(cardModel.getCardAlias());
        card.setAccount(new Account(cardModel.getAccount().getId()));
        card.setCvv(cardModel.getCvv());
        card.setPan(cardModel.getPan());
        cardDAO.save(card);
    }

    @Override
    public void deleteCard(CardModel cardModel) {
        Card card = cardDAO.findFirstByIdAndDateDeletedIsNull(cardModel.getId());
        if(card != null){
            card.setDateDeleted(cardModel.getDateDeleted());
            cardDAO.save(card);
        }
    }
}
