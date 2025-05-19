package com.bank.customer.controllers;

import com.bank.customer.payloads.CustomerCreateRequest;
import com.bank.customer.services.interfaces.CustomerService;
import com.bank.customer.utils.Constants;
import com.bank.customer.utils.Response;
import com.bank.customer.utils.ResponseData;
import com.bank.customer.utils.Util;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody @Valid CustomerCreateRequest customerCreateRequest){
        return ResponseEntity.status(HttpStatus.OK).body(customerService.createUpdate(customerCreateRequest,null));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable UUID id,@RequestBody @Valid CustomerCreateRequest customerCreateRequest){
        ResponseData<?> response = customerService.createUpdate(customerCreateRequest, id);
        if (response.getStatus() == Response.CUSTOMER_NOT_FOUND.status()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(value = "direction", defaultValue = Constants.Pagination.DEFAULT_ORDER_DIRECTION) String direction,
            @RequestParam(value = "orderBy", defaultValue = Constants.Pagination.DEFAULT_ORDER_BY) String orderBy,
            @RequestParam(value = "page", defaultValue = Constants.Pagination.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = Constants.Pagination.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime start = null;
        LocalDateTime end = null;

        try {
            if (startDate != null) {
                start = LocalDate.parse(startDate, formatter).atStartOfDay();
            }else{
                start = LocalDate.of(1990, 1, 1).atStartOfDay();

            }
            if (endDate != null) {
                end = LocalDate.parse(endDate, formatter).plusDays(1).atStartOfDay();
            }else {
                end = LocalDate.now().plusDays(1).atStartOfDay();
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Response.INVALID_DATE_FORMAT.status());
        }
        return ResponseEntity.status(HttpStatus.OK).body(customerService.getCustomers(Util.getPageable(page, size, direction, orderBy),name,start,end));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getOneCustomer(@PathVariable UUID id){
        ResponseData<?> response = customerService.getOne(id);
        if (response.getStatus() == Response.CUSTOMER_NOT_FOUND.status()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable UUID id){
        ResponseData<?> response = customerService.deleteCustomer(id);
        if (response.getStatus() == Response.CUSTOMER_NOT_FOUND.status()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
