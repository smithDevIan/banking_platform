package com.bank.card.controllers;

import com.bank.card.entities.Account;
import com.bank.card.payloads.CardCreateRequest;
import com.bank.card.services.interfaces.CardService;
import com.bank.card.utils.*;
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
class CardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private ObjectMapper objectMapper;

    private CardCreateRequest validRequest;
    private CardCreateRequest invalidRequest;
    private UUID testId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();
        objectMapper = new ObjectMapper();
        Account account = new Account();
        account.setId(UUID.randomUUID());

        testId = UUID.randomUUID();

        validRequest = new CardCreateRequest();
        validRequest.setPan("4111111111111111");
        validRequest.setCardType("virtual");
        validRequest.setCardAlias("My Card");
        validRequest.setAccountId(account.getId());
        validRequest.setCvv(232);

        invalidRequest = new CardCreateRequest(); // remains empty for negative test cases
    }

    @Test
    @DisplayName("POST /api/v1/cards - Success")
    void createCard_WithValidRequest_ReturnsOk() throws Exception {
        when(cardService.createUpdate(any(CardCreateRequest.class), isNull()))
                .thenReturn(new ResponseDataExtended<>(Response.SUCCESS.status(), null));

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("POST /api/v1/cards - Validation Error")
    void createCard_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{id} - Success")
    void updateCard_WithValidRequest_ReturnsOk() throws Exception {
        when(cardService.createUpdate(any(CardCreateRequest.class), eq(testId)))
                .thenReturn(new ResponseDataExtended<>(Response.SUCCESS.status(), null));

        mockMvc.perform(put("/api/v1/cards/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("PUT /api/v1/cards/{id} - Not Found")
    void updateCard_WithNonExistingId_ReturnsNotFound() throws Exception {
        when(cardService.createUpdate(any(CardCreateRequest.class), eq(testId)))
                .thenReturn(new ResponseDataExtended<>(Response.CARD_NOT_FOUND.status(), null));

        mockMvc.perform(put("/api/v1/cards/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/cards - Success")
    void getAllCards_WithDefaultParams_ReturnsOk() throws Exception {
        Page<?> mockPage = new PageImpl<>(Collections.emptyList());
        when(cardService.getAll(any(Pageable.class), isNull(), isNull(), isNull(), anyBoolean()))
                .thenReturn(new PagedResponse<>(Response.SUCCESS.status(), mockPage.getContent(),
                        mockPage.getTotalPages(), mockPage.getSize(), mockPage.getTotalElements()));

        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/cards - With Filters")
    void getAllCards_WithFilters_ReturnsOk() throws Exception {
        Page<?> mockPage = new PageImpl<>(Collections.emptyList());
        when(cardService.getAll(any(Pageable.class), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PagedResponse<>(Response.SUCCESS.status(), mockPage.getContent(),
                        mockPage.getTotalPages(), mockPage.getSize(), mockPage.getTotalElements()));

        mockMvc.perform(get("/api/v1/cards")
                        .param("cardType", "VISA")
                        .param("pan", "411111******1111")
                        .param("cardAlias", "My Card")
                        .param("unMaskPan", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{id} - Success")
    void getOneCard_WithValidId_ReturnsOk() throws Exception {
        when(cardService.getOne(testId, false))
                .thenReturn(new ResponseData<>(Response.SUCCESS.status(), null));

        mockMvc.perform(get("/api/v1/cards/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{id} - With Unmask PAN")
    void getOneCard_WithUnmaskPan_ReturnsOk() throws Exception {
        when(cardService.getOne(testId, true))
                .thenReturn(new ResponseData<>(Response.SUCCESS.status(), null));

        mockMvc.perform(get("/api/v1/cards/{id}", testId)
                        .param("unMaskPan", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("GET /api/v1/cards/{id} - Not Found")
    void getOneCard_WithInvalidId_ReturnsNotFound() throws Exception {
        when(cardService.getOne(testId, false))
                .thenReturn(new ResponseData<>(Response.CARD_NOT_FOUND.status(), null));

        mockMvc.perform(get("/api/v1/cards/{id}", testId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/cards/{id} - Success")
    void deleteCard_WithValidId_ReturnsOk() throws Exception {
        when(cardService.delete(testId))
                .thenReturn(new ResponseData<>(Response.SUCCESS.status(), testId));

        mockMvc.perform(delete("/api/v1/cards/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }

    @Test
    @DisplayName("DELETE /api/v1/cards/{id} - Not Found")
    void deleteCard_WithInvalidId_ReturnsNotFound() throws Exception {
        when(cardService.delete(testId))
                .thenReturn(new ResponseData<>(Response.CARD_NOT_FOUND.status(), null));

        mockMvc.perform(delete("/api/v1/cards/{id}", testId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/cards - Pagination Parameters")
    void getAllCards_WithPaginationParams_ReturnsOk() throws Exception {
        Page<?> mockPage = new PageImpl<>(Collections.emptyList());
        when(cardService.getAll(any(Pageable.class), any(), any(), any(), anyBoolean()))
                .thenReturn(new PagedResponse<>(Response.SUCCESS.status(), mockPage.getContent(),
                        mockPage.getTotalPages(), mockPage.getSize(), mockPage.getTotalElements()));

        mockMvc.perform(get("/api/v1/cards")
                        .param("page", "2")
                        .param("size", "20")
                        .param("direction", "desc")
                        .param("orderBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(Response.SUCCESS.status().getCode()));
    }
}