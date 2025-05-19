package com.bank.card.kafka.models;

import com.bank.card.models.CustomerModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEvent {
    private String event;
    private CustomerModel data;
}
