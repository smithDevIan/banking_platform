package com.bank.customer.services.interfaces;

import com.bank.customer.models.CustomerDataModel;
import com.bank.customer.models.CustomerModel;
import com.bank.customer.payloads.CustomerCreateRequest;
import com.bank.customer.utils.ResponseData;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface CustomerService {
    ResponseData<CustomerModel> createUpdate(CustomerCreateRequest customerCreateRequest, UUID id);

    ResponseData<UUID> deleteCustomer(UUID id);

    Object getCustomers(Pageable pageable, String name, LocalDateTime start, LocalDateTime end);

    ResponseData<CustomerDataModel> getOne(UUID id);
}
