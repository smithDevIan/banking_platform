package com.bank.customer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PagedResponse<T> {
    private ResponseStatus status;
    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    public PagedResponse(ResponseStatus status) {
        this.status = status;
    }
}
