package com.pkozlowski.webstore.service;

import com.pkozlowski.webstore.model.Cart;
import com.pkozlowski.webstore.model.dto.Checkout;

public interface CartService {

    void addItemToCart(long itemId, int quantity);
     Cart getCart();

    void removeCartItem(long id);

    void updateCartItem(long id, int quantity);

    Checkout checkout();
}
