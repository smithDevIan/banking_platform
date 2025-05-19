package com.bank.card.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@Data
public class ResponseError {
    private int code;
    private String message;
    private List<FieldError> errors;

    public ResponseError(ResponseStatus status, List<FieldError> errors){
        this.code = status.getCode();
        this.message = status.getMessage();
        this.errors = errors;
    }
}
