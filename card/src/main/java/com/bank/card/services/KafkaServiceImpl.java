package com.bank.card.services;

import com.bank.card.entities.Account;
import com.bank.card.entities.Customer;
import com.bank.card.models.AccountModel;
import com.bank.card.models.CustomerModel;
import com.bank.card.repositories.AccountDAO;
import com.bank.card.repositories.CustomerDAO;
import com.bank.card.services.interfaces.CardService;
import com.bank.card.services.interfaces.KafkaService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KafkaServiceImpl implements KafkaService {
    private final CustomerDAO customerDAO;
    private final CardService cardService;
    private final AccountDAO accountDAO;

    public KafkaServiceImpl(CustomerDAO customerDAO, CardService cardService, AccountDAO accountDAO) {
        this.customerDAO = customerDAO;
        this.cardService = cardService;
        this.accountDAO = accountDAO;
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
        }
    }

    @Override
    public void createUpdateAccount(AccountModel accountModel) {
        Account account = accountDAO.findFirstByIdAndDateDeletedIsNull(accountModel.getId());
        if (account != null) {
            account.setDateLastUpdated(accountModel.getDateLastUpdated());
        } else {
            account = new Account();
            account.setId(accountModel.getId());
            account.setDateCreated(accountModel.getDateCreated());
        }

        account.setIban(accountModel.getIban());
        account.setBicSwift(accountModel.getBicSwift());
        account.setCustomer(new Customer(accountModel.getCustomer().getId()));
        accountDAO.save(account);
    }

    @Override
    public void deleteAccount(AccountModel accountModel) {
        Account account = accountDAO.findFirstByIdAndDateDeletedIsNull(accountModel.getId());
        if (account != null) {
            account.setDateDeleted(accountModel.getDateDeleted());
            accountDAO.save(account);

            cardService.deleteAllAccountCards(account);
        }
    }
}
