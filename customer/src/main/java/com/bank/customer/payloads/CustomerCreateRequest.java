package com.bank.customer.payloads;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCreateRequest {
    @NotNull(message = "{field.required}")
    private String firstName;
    @NotNull(message = "{field.required}")
    private String lastName;
    private String otherName;
}
