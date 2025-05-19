package com.bank.card.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ResponseDataExtended<T>  extends ResponseData<T> {
    private List<FieldError> errors;
    public ResponseDataExtended(ResponseStatus status, T data, List<FieldError> errors) {
        super(status, data);
        this.errors = errors;
    }

    public ResponseDataExtended(ResponseStatus status, T data) {
        super(status, data);
    }

    public ResponseDataExtended(ResponseStatus status) {
        this.status = status;
    }
}
