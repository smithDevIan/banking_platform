package com.bank.customer.utils;

public enum Response {
    SUCCESS(0, "success"),
    FAILED(1, "failed"),
    ERRORS_OCCURRED(2,"Errors occurred"),
    CUSTOMER_NOT_FOUND(3,"Customer not found"),
    CUSTOMER_DELETED(4,"Customer deleted successfully"),
    ACCOUNT_NOT_FOUND(6,"Account not found"),
    ACCOUNT_OWNERSHIP_FIXED(7, "Account ownership once set cannot be changed"),
    INVALID_IBAN(8, "Invalid IBAN"),
    INVALID_BIC_SWIFT(9, "Invalid BicSwift"),
    ACCOUNT_DELETED(10,"Account has been deleted");
    private final ResponseStatus status;
    Response(int code, String message){this.status = new ResponseStatus(code,message);}

    public ResponseStatus status(){return status;}
}
