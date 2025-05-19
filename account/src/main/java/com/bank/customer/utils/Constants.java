package com.bank.customer.utils;

public interface Constants {
    class Kafka {
        public static final String CREATE_UPDATE = "create_update";
        public static final String DELETE = "delete";
    }

    public interface Pagination {
        String DEFAULT_ORDER_DIRECTION = "DESC";
        String DEFAULT_PAGE_NUMBER = "0";
        String DEFAULT_PAGE_SIZE = "2147483647";
        String DEFAULT_ORDER_BY = "dateCreated";
    }
}
