package com.bank.customer.controllers;

import com.bank.customer.models.CustomerDataModel;
import com.bank.customer.models.CustomerModel;
import com.bank.customer.payloads.CustomerCreateRequest;
import com.bank.customer.services.interfaces.CustomerService;
import com.bank.customer.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private ObjectMapper objectMapper;

    private CustomerCreateRequest validRequest;
    private CustomerCreateRequest invalidRequest;
    private UUID testId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
        objectMapper = new ObjectMapper();

        testId = UUID.randomUUID();
        validRequest = new CustomerCreateRequest("John", "Doe", "Middle");
        invalidRequest = new CustomerCreateRequest(null, null, null);
    }

    @Test
    @DisplayName("POST /api/v1/customers - Success")
    void createCustomer_WithValidRequest_ReturnsOk() throws Exception {
        when(customerService.createUpdate(any(CustomerCreateRequest.class), isNull()))
                .thenReturn(new ResponseData<>(Response.SUCCESS.status(), new CustomerModel()));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("POST /api/v1/customers - Validation Error")
    void createCustomer_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{id} - Success")
    void updateCustomer_WithValidRequest_ReturnsOk() throws Exception {
        when(customerService.createUpdate(any(CustomerCreateRequest.class), eq(testId)))
                .thenReturn(new ResponseData<>(Response.SUCCESS.status(), new CustomerModel()));

        mockMvc.perform(put("/api/v1/customers/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{id} - Not Found")
    void updateCustomer_WithNonExistingId_ReturnsNotFound() throws Exception {
        when(customerService.createUpdate(any(CustomerCreateRequest.class), eq(testId)))
                .thenReturn(new ResponseData<>(Response.CUSTOMER_NOT_FOUND.status(), null));

        mockMvc.perform(put("/api/v1/customers/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/customers - Success")
    void getAllCustomers_WithDefaultParams_ReturnsOk() throws Exception {
        Page<CustomerModel> mockPage = new PageImpl<>(Collections.emptyList());
        when(customerService.getCustomers(any(Pageable.class), any(), any(), any()))
                .thenReturn(new PagedResponse<>(Response.SUCCESS.status(), mockPage.getContent(),
                        mockPage.getTotalPages(), mockPage.getSize(), mockPage.getTotalElements()));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - Success")
    void getOneCustomer_WithValidId_ReturnsOk() throws Exception {
        when(customerService.getOne(testId))
                .thenReturn(new ResponseData<>(Response.SUCCESS.status(), new CustomerDataModel()));

        mockMvc.perform(get("/api/v1/customers/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - Not Found")
    void getOneCustomer_WithInvalidId_ReturnsNotFound() throws Exception {
        when(customerService.getOne(testId))
                .thenReturn(new ResponseData<>(Response.CUSTOMER_NOT_FOUND.status(), null));

        mockMvc.perform(get("/api/v1/customers/{id}", testId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{id} - Success")
    void deleteCustomer_WithValidId_ReturnsOk() throws Exception {
        when(customerService.deleteCustomer(testId))
                .thenReturn(new ResponseData<>(Response.CUSTOMER_DELETED.status(), testId));

        mockMvc.perform(delete("/api/v1/customers/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.CUSTOMER_DELETED.status().getCode()));
    }

    @Test
    @DisplayName("DELETE /api/v1/customers/{id} - Not Found")
    void deleteCustomer_WithInvalidId_ReturnsNotFound() throws Exception {
        when(customerService.deleteCustomer(testId))
                .thenReturn(new ResponseData<>(Response.CUSTOMER_NOT_FOUND.status(), testId));

        mockMvc.perform(delete("/api/v1/customers/{id}", testId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/customers - With Filters")
    void getAllCustomers_WithFilters_ReturnsOk() throws Exception {
        Page<CustomerModel> mockPage = new PageImpl<>(Collections.emptyList());
        when(customerService.getCustomers(any(Pageable.class), any(), any(), any()))
                .thenReturn(new PagedResponse<>(Response.SUCCESS.status(), mockPage.getContent(),
                        mockPage.getTotalPages(), mockPage.getSize(), mockPage.getTotalElements()));

        mockMvc.perform(get("/api/v1/customers")
                        .param("name", "John")
                        .param("startDate", "01/01/2023")
                        .param("endDate", "01/01/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/customers - Invalid Date Format")
    void getAllCustomers_WithInvalidDateFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/customers")
                        .param("startDate", "01-01-2023")  // Invalid format
                        .param("endDate", "31-12-2023"))   // Invalid format
                .andExpect(status().isBadRequest());
    }
}