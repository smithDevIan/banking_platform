package com.bank.card.kafka;

import com.bank.card.kafka.models.AccountEvent;
import com.bank.card.kafka.models.CustomerEvent;
import com.bank.card.models.AccountModel;
import com.bank.card.models.CustomerModel;
import com.bank.card.services.interfaces.KafkaService;
import com.bank.card.utils.Constants;
import com.bank.card.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class MessageListener {
    private final KafkaService kafkaService;

    public MessageListener(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @KafkaListener(topics = "${customer.topic}")
    public void listenCustomer(String message){
        log.info("Customer Received data {}",message);
        CustomerEvent event = Util.fromJson(message, CustomerEvent.class);
        CustomerModel customerModel = event != null ? event.getData() : null;
        if(customerModel != null){
            if(event.getEvent().equalsIgnoreCase(Constants.Kafka.CREATE_UPDATE)) {
                kafkaService.createUpdateCustomer(customerModel);
            }else{
                kafkaService.deleteCustomer(customerModel);
            }
        }
    }

    @KafkaListener(topics = "${account.topic}")
    public void listenAccount(String message){
        log.info("Account Received data {}",message);
        AccountEvent event = Util.fromJson(message, AccountEvent.class);
        AccountModel accountModel = event != null ? event.getData() : null;
        if(accountModel != null){
            if(event.getEvent().equalsIgnoreCase(Constants.Kafka.CREATE_UPDATE)) {
                kafkaService.createUpdateAccount(accountModel);
            }else{
                kafkaService.deleteAccount(accountModel);
            }
        }
    }
}
