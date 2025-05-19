package com.bank.customer.kafka.models;

import com.bank.customer.models.CustomerModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardEvent {
    private String event;
    private CardModel data;
}
