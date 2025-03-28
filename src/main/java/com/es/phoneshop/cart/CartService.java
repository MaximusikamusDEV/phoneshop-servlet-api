package com.es.phoneshop.cart;

import com.es.phoneshop.exceptions.ProductNotFoundException;
import com.es.phoneshop.exceptions.ProductOutOfStockException;
import jakarta.servlet.http.HttpServletRequest;

public interface CartService {
    Cart getCart(HttpServletRequest request);
    void add(Cart cart, Long productId, int quantity) throws ProductOutOfStockException;
    void update(Cart cart, Long productId, int quantity) throws ProductOutOfStockException, ProductNotFoundException;
    void delete(Cart cart, Long productId);
}
