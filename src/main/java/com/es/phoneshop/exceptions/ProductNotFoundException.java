package com.es.phoneshop.exceptions;

public class ProductNotFoundException extends Exception {
    Long productId;

    public ProductNotFoundException(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    @Override
    public String toString() {
        return "ProductNotFoundException {productId= " + productId + '}';
    }
}
