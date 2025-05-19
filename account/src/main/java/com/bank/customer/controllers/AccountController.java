package com.bank.customer.controllers;

import com.bank.customer.payloads.AccountCreateRequest;
import com.bank.customer.services.interfaces.AccountService;
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
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid AccountCreateRequest accountCreateRequest){
        return ResponseEntity.status(HttpStatus.OK).body(accountService.createUpdate(accountCreateRequest,null));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable UUID id,@RequestBody @Valid AccountCreateRequest accountCreateRequest){
        ResponseData<?> response = accountService.createUpdate(accountCreateRequest,id);
        if (response.getStatus() == Response.ACCOUNT_NOT_FOUND.status()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(value = "direction", defaultValue = Constants.Pagination.DEFAULT_ORDER_DIRECTION) String direction,
            @RequestParam(value = "orderBy", defaultValue = Constants.Pagination.DEFAULT_ORDER_BY) String orderBy,
            @RequestParam(value = "page", defaultValue = Constants.Pagination.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = Constants.Pagination.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "iban", required = false) String iban,
            @RequestParam(value = "bicSwift", required = false) String bicSwift,
            @RequestParam(value = "cardAlias", required = false) String cardAlias
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAll(Util.getPageable(page, size, direction, orderBy),iban,bicSwift,cardAlias));
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getOne(@PathVariable UUID id){
        ResponseData<?> response = accountService.getOne(id);
        if (response.getStatus() == Response.ACCOUNT_NOT_FOUND.status()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id){
        ResponseData<?> response = accountService.delete(id);
        if (response.getStatus() == Response.ACCOUNT_NOT_FOUND.status()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
