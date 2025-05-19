package com.bank.customer.controllers;

import com.bank.customer.models.AccountDetailedModel;
import com.bank.customer.models.AccountModel;
import com.bank.customer.payloads.AccountCreateRequest;
import com.bank.customer.services.interfaces.AccountService;
import com.bank.customer.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private ObjectMapper objectMapper;

    private AccountCreateRequest validRequest;
    private AccountCreateRequest invalidRequest;
    private UUID testId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        objectMapper = new ObjectMapper();

        testId = UUID.randomUUID();

        validRequest = new AccountCreateRequest();
        validRequest.setIban("DE89370400440532013000");
        validRequest.setBicSwift("DEUTDEFF");

        validRequest.setCustomerId(UUID.randomUUID());

        invalidRequest = new AccountCreateRequest(); // remains empty for negative test cases
    }


    @Test
    @DisplayName("POST /api/v1/accounts - Success")
    void createAccount_WithValidRequest_ReturnsOk() throws Exception {
        when(accountService.createUpdate(any(AccountCreateRequest.class), isNull()))
                .thenReturn(new ResponseData<>(Response.SUCCESS.status(), new AccountModel()));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("POST /api/v1/accounts - Validation Error")
    void createAccount_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/accounts/{id} - Success")
    void updateAccount_WithValidRequest_ReturnsOk() throws Exception {
        when(accountService.createUpdate(any(AccountCreateRequest.class), eq(testId)))
                .thenReturn(new ResponseData<>(Response.SUCCESS.status(), new AccountModel()));

        mockMvc.perform(put("/api/v1/accounts/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("PUT /api/v1/accounts/{id} - Not Found")
    void updateAccount_WithNonExistingId_ReturnsNotFound() throws Exception {
        when(accountService.createUpdate(any(AccountCreateRequest.class), eq(testId)))
                .thenReturn(new ResponseData<>(Response.ACCOUNT_NOT_FOUND.status(), null));

        mockMvc.perform(put("/api/v1/accounts/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/accounts - Success")
    void getAllAccounts_WithDefaultParams_ReturnsOk() throws Exception {
        Page<AccountModel> mockPage = new PageImpl<>(Collections.emptyList());
        when(accountService.getAll(any(Pageable.class), isNull(), isNull(), isNull()))
                .thenReturn(new PagedResponse<>(Response.SUCCESS.status(), mockPage.getContent(),
                        mockPage.getTotalPages(), mockPage.getSize(), mockPage.getTotalElements()));

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/accounts - With Filters")
    void getAllAccounts_WithFilters_ReturnsOk() throws Exception {
        Page<AccountModel> mockPage = new PageImpl<>(Collections.emptyList());
        when(accountService.getAll(any(Pageable.class), any(), any(), any()))
                .thenReturn(new PagedResponse<>(Response.SUCCESS.status(), mockPage.getContent(),
                        mockPage.getTotalPages(), mockPage.getSize(), mockPage.getTotalElements()));

        mockMvc.perform(get("/api/v1/accounts")
                        .param("iban", "DE89370400440532013000")
                        .param("bicSwift", "DEUTDEFF")
                        .param("cardAlias", "My Card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} - Success")
    void getOneAccount_WithValidId_ReturnsOk() throws Exception {
        when(accountService.getOne(testId))
                .thenReturn(new ResponseData<>(Response.SUCCESS.status(), new AccountDetailedModel()));

        mockMvc.perform(get("/api/v1/accounts/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} - Not Found")
    void getOneAccount_WithInvalidId_ReturnsNotFound() throws Exception {
        when(accountService.getOne(testId))
                .thenReturn(new ResponseData<>(Response.ACCOUNT_NOT_FOUND.status(), null));

        mockMvc.perform(get("/api/v1/accounts/{id}", testId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id} - Success")
    void deleteAccount_WithValidId_ReturnsOk() throws Exception {
        when(accountService.delete(testId))
                .thenReturn(new ResponseData<>(Response.ACCOUNT_DELETED.status(), testId));

        mockMvc.perform(delete("/api/v1/accounts/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.ACCOUNT_DELETED.status().getCode()));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id} - Not Found")
    void deleteAccount_WithInvalidId_ReturnsNotFound() throws Exception {
        when(accountService.delete(testId))
                .thenReturn(new ResponseData<>(Response.ACCOUNT_NOT_FOUND.status(), testId));

        mockMvc.perform(delete("/api/v1/accounts/{id}", testId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/accounts - Pagination Parameters")
    void getAllAccounts_WithPaginationParams_ReturnsOk() throws Exception {
        Page<AccountModel> mockPage = new PageImpl<>(Collections.emptyList());
        when(accountService.getAll(any(Pageable.class), any(), any(), any()))
                .thenReturn(new PagedResponse<>(Response.SUCCESS.status(), mockPage.getContent(),
                        mockPage.getTotalPages(), mockPage.getSize(), mockPage.getTotalElements()));

        mockMvc.perform(get("/api/v1/accounts")
                        .param("page", "2")
                        .param("size", "20")
                        .param("direction", "desc")
                        .param("orderBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }
}