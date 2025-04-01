package com.es.phoneshop.cart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {
    List<CartItem> cartItems;

    public Cart() {
        cartItems = new ArrayList<>();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @Override
    public String toString() {
        return "Cart{ cartItems= " + cartItems + '}';
    }
}
