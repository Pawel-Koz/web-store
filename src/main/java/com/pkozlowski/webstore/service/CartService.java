package com.pkozlowski.webstore.service;

import com.pkozlowski.webstore.model.Cart;

public interface CartService {

    void addItemToCart(long itemId, int quantity);
     Cart getCart();

    void removeCartItem(long id);

    void updateItem(long id, int quantity);
}
