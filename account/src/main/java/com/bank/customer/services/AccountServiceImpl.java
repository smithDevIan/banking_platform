package com.bank.customer.services;

import com.bank.customer.entities.Account;
import com.bank.customer.entities.Card;
import com.bank.customer.entities.Customer;
import com.bank.customer.kafka.MessageProducer;
import com.bank.customer.kafka.models.ProducerEvent;
import com.bank.customer.models.*;
import com.bank.customer.payloads.AccountCreateRequest;
import com.bank.customer.repositories.AccountDAO;
import com.bank.customer.repositories.CardDAO;
import com.bank.customer.repositories.CustomerDAO;
import com.bank.customer.services.interfaces.AccountService;
import com.bank.customer.utils.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {
    @Value("${account.topic}")
    private String accountTopic;
    private final AccountDAO accountDAO;
    private final CustomerDAO customerDAO;
    private final CardDAO cardDAO;
    private final MessageProducer messageProducer;

    public AccountServiceImpl(AccountDAO accountDAO, CustomerDAO customerDAO, CardDAO cardDAO, MessageProducer messageProducer) {
        this.accountDAO = accountDAO;
        this.customerDAO = customerDAO;
        this.cardDAO = cardDAO;
        this.messageProducer = messageProducer;
    }

    @Override
    public ResponseData<Object> createUpdate(AccountCreateRequest request, UUID id) {
        AccountModel accountModel = null;
        ValidationResult validation = validate(request, id);

        if (validation.errors.isEmpty()) {

            Account account = validation.account;
            Customer customer = validation.customer;

            if (account != null) {
                account.setDateLastUpdated(LocalDateTime.now());
            } else {
                account = new Account();
                account.setDateCreated(LocalDateTime.now());
            }

            account.setIban(request.getIban());
            account.setBicSwift(request.getBicSwift());
            account.setCustomer(customer);
            accountDAO.save(account);
            accountModel = new AccountModel(account);

            sendAccountData(accountModel, Constants.Kafka.CREATE_UPDATE);
        }
        return validation.errors.isEmpty() ? new ResponseData<>(Response.SUCCESS.status(), accountModel):new ResponseData<>(Response.ERRORS_OCCURRED.status(),validation.getErrors());
    }

    public void sendAccountData(AccountModel accountModel, String event) {
        ProducerEvent producerEvent = new ProducerEvent(event,accountModel);
        messageProducer.publish(accountTopic,Util.toJson(producerEvent));
    }

    public ValidationResult validate(AccountCreateRequest request, UUID id) {
        ValidationResult result = new ValidationResult();

        if (!Util.isValid(request.getIban())) {
            result.status = Response.INVALID_IBAN.status();
            result.errors.add(new FieldError("iban", result.status.getMessage()));
        }

        if (!Util.isValidBicSwift(request.getBicSwift())) {
            result.status = Response.INVALID_BIC_SWIFT.status();
            result.errors.add(new FieldError("bicSwift", result.status.getMessage()));
        }

        result.customer = customerDAO.findFirstByIdAndDateDeletedIsNull(request.getCustomerId());
        if (result.customer == null) {
            result.status = Response.CUSTOMER_NOT_FOUND.status();
            result.errors.add(new FieldError("customerId", result.status.getMessage()));
        }

        if (id != null) {
            result.account = accountDAO.findFirstByIdAndDateDeletedIsNull(id);
            if (result.account == null) {
                result.status = Response.ACCOUNT_NOT_FOUND.status();
                result.errors.add(new FieldError("id", result.status.getMessage()));
            } else if (!result.account.getCustomer().getId().equals(request.getCustomerId())) {
                result.status = Response.ACCOUNT_OWNERSHIP_FIXED.status();
                result.errors.add(new FieldError("customerId", result.status.getMessage()));
            }
        }

        return result;
    }


    @Override
    public Object getAll(Pageable pageable, String iban, String bicSwift, String cardAlias) {
        String alias = Util.processSearchName(cardAlias);
        Page<Account> accounts = accountDAO.findAllByIbanAndBicSwiftAndCardAliasAndDateDeletedIsNull(iban,bicSwift,alias,pageable);
        return new PagedResponse<>(Response.SUCCESS.status(),accounts.getContent().stream().map(AccountModel::new).toList(),accounts.getTotalPages(),accounts.getSize(),accounts.getTotalElements());
    }

    @Override
    public ResponseData<AccountDetailedModel> getOne(UUID id) {
        ResponseStatus status;
        Account account = accountDAO.findFirstByIdAndDateDeletedIsNull(id);
        AccountDetailedModel accountModel = null;
        if(account != null){
            List<Card> cards = cardDAO.findAllByAccountAndDateDeletedIsNullOrderByDateCreatedDesc(account);
            List<CardShortModel> cardModels = cards.stream()
                    .map(card -> new CardShortModel(card, false))
                    .toList();
            accountModel = new AccountDetailedModel(account,cardModels);
            status = Response.SUCCESS.status();
        }else {
            status =Response.ACCOUNT_NOT_FOUND.status();
        }
        return new ResponseData<>(status,accountModel);
    }

    @Override
    public ResponseData<UUID> delete(UUID id) {
        ResponseStatus status;
        Account account = accountDAO.findFirstByIdAndDateDeletedIsNull(id);
        if(account !=  null){
            account.setDateDeleted(LocalDateTime.now());
            accountDAO.save(account);
            AccountModel accountModel = new AccountModel(account);
            status = Response.ACCOUNT_DELETED.status();

            //Send customer data to other microservices using kafka
            sendAccountData(accountModel,Constants.Kafka.DELETE);
        }else {
            status = Response.ACCOUNT_NOT_FOUND.status();
        }
        return new ResponseData<>(status,id);
    }

    @Override
    public void deleteCustomerAccounts(Customer customer) {
        List<Account> accounts = accountDAO.findAllByCustomerAndDateDeletedIsNull(customer);
        for (Account account:accounts){
            delete(account.getId());
        }
    }
}
