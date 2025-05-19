package com.bank.customer.payloads;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {
    @NotNull(message = "{field.required}")
    private String iban;
    @NotNull(message = "{field.required}")
    private String bicSwift;
    @NotNull(message = "{field.required}")
    private UUID customerId;
}
