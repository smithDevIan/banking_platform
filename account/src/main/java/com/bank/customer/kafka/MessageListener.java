package com.bank.customer.kafka;

import com.bank.customer.kafka.models.CardEvent;
import com.bank.customer.kafka.models.CardModel;
import com.bank.customer.kafka.models.CustomerEvent;
import com.bank.customer.models.CustomerModel;
import com.bank.customer.services.interfaces.KafkaService;
import com.bank.customer.utils.Constants;
import com.bank.customer.utils.Util;
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

    @KafkaListener(topics = "${card.topic}")
    public void listenCard(String message){
        log.info("Card Received data {}",message);
        CardEvent event = Util.fromJson(message, CardEvent.class);
        CardModel cardModel = event != null ? event.getData() : null;
        if(cardModel != null){
            if(event.getEvent().equalsIgnoreCase(Constants.Kafka.CREATE_UPDATE)) {
                kafkaService.createUpdateCard(cardModel);
            }else{
                kafkaService.deleteCard(cardModel);
            }
        }
    }
}
