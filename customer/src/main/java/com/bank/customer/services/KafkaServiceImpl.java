package com.bank.customer.services;

import com.bank.customer.entities.Account;
import com.bank.customer.entities.Card;
import com.bank.customer.entities.CardType;
import com.bank.customer.entities.Customer;
import com.bank.customer.kafka.models.AccountModel;
import com.bank.customer.kafka.models.CardModel;
import com.bank.customer.repositories.AccountDAO;
import com.bank.customer.repositories.CardDAO;
import com.bank.customer.services.interfaces.KafkaService;
import org.springframework.stereotype.Service;

@Service
public class KafkaServiceImpl implements KafkaService {
    private final AccountDAO accountDAO;
    private final CardDAO cardDAO;

    public KafkaServiceImpl(AccountDAO accountDAO, CardDAO cardDAO) {
        this.accountDAO = accountDAO;
        this.cardDAO = cardDAO;
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
