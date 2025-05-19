package com.bank.customer.models;

import com.bank.customer.entities.Account;
import com.bank.customer.entities.Customer;
import com.bank.customer.utils.FieldError;
import com.bank.customer.utils.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResult {
    public List<FieldError> errors = new ArrayList<>();
    public ResponseStatus status;
    public Account account;
    public Customer customer;
}
