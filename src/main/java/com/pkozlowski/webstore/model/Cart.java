package com.pkozlowski.webstore.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
    private List<CartItem> cartItems = new ArrayList<>();
    private BigDecimal subtotal;


    public void addCartItem(CartItem cartItem) {
       cartItems.add(cartItem);
    }
}
