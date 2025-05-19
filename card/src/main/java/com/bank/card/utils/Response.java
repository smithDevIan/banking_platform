package com.bank.card.utils;

public enum Response {
    SUCCESS(0, "Success"),
    FAILED(1, "Failed"),
    ERRORS_OCCURRED(2,"Errors occurred"),
    CUSTOMER_NOT_FOUND(3,"Customer not found"),
    CUSTOMER_DELETED(4,"Customer has been deleted"),
    INVALID_DATE_FORMAT(5,"Invalid date format.Please use dd/mm/yyyy"),
    ACCOUNT_NOT_FOUND(6,"Account not found"),
    ACCOUNT_OWNERSHIP_FIXED(7, "Account ownership once set cannot be changed"),
    INVALID_PAN(8, "Invalid PAN"),
    INVALID_BIC_SWIFT(9, "Invalid BicSwift"),
    CARD_TYPE_NOT_FOUND(10, "Card type not found"),
    INVALID_CVV(11, "Invalid cvv"),
    CARD_NOT_FOUND(12, "Card not found"),
    CARD_TYPE_ACCOUNT_EXISTS(13,"Card details of account {0} with card type {1} already exist.Create using a different card type."),
    CARD_DELETED(14, "Card has been deleted");
    private final ResponseStatus status;
    Response(int code, String message){this.status = new ResponseStatus(code,message);}

    public ResponseStatus status(){return status;}
}
