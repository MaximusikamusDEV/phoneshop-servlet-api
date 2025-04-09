package com.es.phoneshop.exceptions;

public class OrderNotFoundException extends Exception {
    String orderId;

    public OrderNotFoundException(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}
