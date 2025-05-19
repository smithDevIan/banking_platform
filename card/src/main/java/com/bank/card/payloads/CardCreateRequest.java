package com.bank.card.payloads;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardCreateRequest {
    @NotNull(message = "{field.required}")
    private String pan;
    @NotNull(message = "{field.required}")
    private String cardAlias;
    @NotNull(message = "{field.required}")
    private UUID accountId;
    @NotNull(message = "{field.required}")
    private int cvv;
    @NotNull(message = "{field.required}")
    private String cardType;
}
