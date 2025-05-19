package com.bank.customer.services;

import com.bank.customer.entities.Account;
import com.bank.customer.entities.Card;
import com.bank.customer.entities.Customer;
import com.bank.customer.kafka.MessageProducer;
import com.bank.customer.kafka.models.ProducerEvent;
import com.bank.customer.models.AccountDetailedModel;
import com.bank.customer.models.CardShortModel;
import com.bank.customer.models.CustomerDataModel;
import com.bank.customer.models.CustomerModel;
import com.bank.customer.payloads.CustomerCreateRequest;
import com.bank.customer.repositories.AccountDAO;
import com.bank.customer.repositories.CardDAO;
import com.bank.customer.repositories.CustomerDAO;
import com.bank.customer.services.interfaces.CustomerService;
import com.bank.customer.utils.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Log4j2
@Service
public class CustomerServiceImpl implements CustomerService {
    @Value("${customer.topic}")
    private String customerTopic;
    private final CustomerDAO customerDAO;
    private final CardDAO cardDAO;
    private final AccountDAO accountDAO;
    private final MessageProducer messageProducer;

    public CustomerServiceImpl(CustomerDAO customerDAO, CardDAO cardDAO, AccountDAO accountDAO, MessageProducer messageProducer) {
        this.customerDAO = customerDAO;
        this.cardDAO = cardDAO;
        this.accountDAO = accountDAO;
        this.messageProducer = messageProducer;
    }

    @Override
    public  ResponseData<CustomerModel> createUpdate(CustomerCreateRequest customerCreateRequest, UUID id) {
        ResponseStatus status = null;
        Customer customer = null;
        CustomerModel customerModel = null;
        if(id != null){
            customer = customerDAO.findFirstByIdAndDateDeletedIsNull(id);
            if(customer == null){
                status = Response.CUSTOMER_NOT_FOUND.status();
            }
        }
        if(status == null){
            if(customer != null){
                customer.setDateLastUpdated(LocalDateTime.now());
            }else{
                customer = new Customer();
                customer.setDateCreated(LocalDateTime.now());
            }
            customer.setFirstName(customerCreateRequest.getFirstName());
            customer.setLastName(customerCreateRequest.getLastName());
            customer.setOtherName(customerCreateRequest.getOtherName());
            customerDAO.save(customer);
            customerModel = new CustomerModel(customer);
            status = Response.SUCCESS.status();

            //Send customer data to other microservices using kafka
            sendCustomerData(customerModel,Constants.Kafka.CREATE_UPDATE);
        }
        return new ResponseData<>(status,customerModel);
    }

    @Override
    public Object getCustomers(Pageable pageable, String nameSearch, LocalDateTime start, LocalDateTime end) {
        String name = Util.processSearchName(nameSearch);
        Page<Customer> customers = customerDAO.findAllByNameOrDateRangeWhereDateDeletedIsNull(name,start,end,pageable);
        return new PagedResponse<>(Response.SUCCESS.status(),customers.getContent().stream().map(CustomerModel::new).toList(),customers.getTotalPages(),customers.getSize(),customers.getTotalElements());
    }

    @Override
    public ResponseData<CustomerDataModel> getOne(UUID id) {
        Customer customer = customerDAO.findFirstByIdAndDateDeletedIsNull(id);
        if (customer == null) {
            return new ResponseData<>(Response.CUSTOMER_NOT_FOUND.status(), null);
        }

        List<AccountDetailedModel> accountDetails = getAccountDetailsForCustomer(customer);
        CustomerDataModel customerDataModel = new CustomerDataModel(customer, accountDetails);

        return new ResponseData<>(Response.SUCCESS.status(), customerDataModel);
    }

    private List<AccountDetailedModel> getAccountDetailsForCustomer(Customer customer) {
        List<Account> accounts = accountDAO.findAllByCustomerAndDateDeletedIsNullOrderByDateCreatedDesc(customer);

        return accounts.stream()
                .map(account -> {
                    List<CardShortModel> cardModels = getCardModelsForAccount(account);
                    return new AccountDetailedModel(account, cardModels);
                })
                .toList();
    }

    private List<CardShortModel> getCardModelsForAccount(Account account) {
        List<Card> cards = cardDAO.findAllByAccountAndDateDeletedIsNullOrderByDateCreatedDesc(account);
        return cards.stream()
                .map(card -> new CardShortModel(card, false))
                .toList();
    }


    @Override
    public ResponseData<UUID> deleteCustomer(UUID id) {
        ResponseStatus status;
        Customer customer = customerDAO.findFirstByIdAndDateDeletedIsNull(id);
        if(customer !=  null){
            customer.setFirstName("deleted");
            customer.setLastName("deleted");
            customer.setOtherName("deleted");
            customer.setDateDeleted(LocalDateTime.now());
            customerDAO.save(customer);
            CustomerModel customerModel = new CustomerModel(customer);
            status = Response.CUSTOMER_DELETED.status();

            //Send customer data to other microservices using kafka
            sendCustomerData(customerModel,Constants.Kafka.DELETE);
        }else {
            status = Response.CUSTOMER_NOT_FOUND.status();
        }
        return new ResponseData<>(status,id);
    }

    private void sendCustomerData(CustomerModel customerModel,String event) {
        ProducerEvent producerEvent = new ProducerEvent(event,customerModel);
        messageProducer.publish(customerTopic,Util.toJson(producerEvent));
    }
}
