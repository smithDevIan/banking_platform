package com.bank.customer.utils;

import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class ResponseData<T> {
    protected ResponseStatus status;
    protected T data;

    public ResponseData(ResponseStatus status, T data) {
        this.status = status;
        this.data = data;
    }

    public ResponseData(ResponseStatus status) {
        this.status = status;
    }
}
