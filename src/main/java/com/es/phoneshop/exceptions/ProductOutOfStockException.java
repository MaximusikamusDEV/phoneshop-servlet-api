package com.es.phoneshop.exceptions;

import com.es.phoneshop.model.product.Product;

public class ProductOutOfStockException extends Exception {
    private final Product product;
    private final int availableStock;
    private final int requiredStock;

    public ProductOutOfStockException(Product product, int availableStock, int requiredStock) {
        this.product = product;
        this.availableStock = availableStock;
        this.requiredStock = requiredStock;
    }

    public Product getProduct() {
        return product;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getRequiredStock() {
        return requiredStock;
    }
}
