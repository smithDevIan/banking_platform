package com.bank.customer.utils;

public enum Response {
    SUCCESS(0, "Success"),
    FAILED(1, "Failed"),
    ERRORS_OCCURRED(2,"Errors occurred"),
    CUSTOMER_NOT_FOUND(404,"Customer not found"),
    CUSTOMER_DELETED(4,"Customer has been deleted successfully"),
    CUSTOMER_CREATED(5, "Customer created successfully"),
    CUSTOMER_UPDATED(6, "Customer updated successfully"),
    CUSTOMERS_RETRIEVED(7, "Customers retrieved successfully"),
    INVALID_DATE_FORMAT(8,"Invalid date format. Please use dd/MM/yyyy");
    private final ResponseStatus status;
    Response(int code, String message){this.status = new ResponseStatus(code,message);}

    public ResponseStatus status(){return status;}
}
