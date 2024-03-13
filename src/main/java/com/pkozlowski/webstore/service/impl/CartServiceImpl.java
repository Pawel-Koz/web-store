package com.pkozlowski.webstore.service.impl;

import com.pkozlowski.webstore.model.Cart;
import com.pkozlowski.webstore.model.CartItem;
import com.pkozlowski.webstore.model.Item;
import com.pkozlowski.webstore.exception.ItemException;
import com.pkozlowski.webstore.repository.ItemRepository;
import com.pkozlowski.webstore.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    private final ItemRepository itemRepository;
    private final HttpSession session;

    public CartServiceImpl(ItemRepository itemRepository, HttpSession httpSession) {
        this.itemRepository = itemRepository;
        this.session = httpSession;
    }

    @Override
    public void addItemToCart(long itemId, int quantity) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemException(String.format("Item with id: %d not available", itemId)));
        if (quantity > item.getAvailable()) {
            throw new RuntimeException(String.format(
                    "Available item: %s - number of items: %d, requested:%d",
                    item.getTitle(), item.getAvailable(), quantity));
        }
        //TODO: check if needed
        //UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        Optional<CartItem> existingCartItem = cart.getCartItems().stream().filter(itemInCart -> itemInCart.getTitle().equals(item.getTitle()))
                .findFirst();
        if (existingCartItem.isPresent()) {
            int setQuantity = existingCartItem.get().getQuantity() + quantity;
            existingCartItem.get().setQuantity(setQuantity);
            existingCartItem.get().setPrice(existingCartItem.get().getPricePerUnit().multiply(new BigDecimal(setQuantity)));
        } else {
            CartItem cartItem = buildCartItem(quantity, item);
            cart.addCartItem(cartItem);
        }
        item.setAvailable(item.getAvailable() - quantity);
        itemRepository.save(item);
    }

    @Override
    public Cart getCart() {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            return new Cart();
        }
        int ordinal = 1;
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            item.setOrdinal(ordinal++);
            total = total.add(item.getPrice());
        }
        cart.setSubtotal(total);
        return cart;
    }

    @Override
    public void removeCartItem(long id) {
        Cart cart = getCartFromSession();
        CartItem itemToDelete = getCartItemFromCart(id, cart);
        removeItemFromCartUpdateDb(id, cart, itemToDelete);
    }

    @Override
    public void updateItem(long id, int quantity) {
        Cart cart = getCartFromSession();
        CartItem cartItem = getCartItemFromCart(id, cart);

        Item item = itemRepository.getReferenceById(id);
        int actualItemsQuantity = cartItem.getQuantity();
        int quantityToSet;
        if (quantity == 0) {
            removeItemFromCartUpdateDb(id, cart, cartItem);
        } else if (quantity < actualItemsQuantity) {
            quantityToSet = actualItemsQuantity - quantity;
            cartItem.setQuantity(quantity);
            cartItem.setPrice(cartItem.getPricePerUnit().multiply(new BigDecimal(quantity)));
            item.setAvailable(item.getAvailable() + quantityToSet);
        } else if (quantity > actualItemsQuantity) {
            quantityToSet = quantity - actualItemsQuantity;
            cartItem.setQuantity(quantity);
            cartItem.setPrice(cartItem.getPricePerUnit().multiply(new BigDecimal(quantity)));
            item.setAvailable(item.getAvailable() - quantityToSet);
        }
        itemRepository.save(item);
    }

    private static CartItem buildCartItem(int quantity, Item item) {
        return CartItem.builder()
                .id(item.getId())
                .title(item.getTitle())
                .pricePerUnit(item.getPrice())
                .price(item.getPrice().multiply(new BigDecimal(quantity)))
                .quantity(quantity)
                .build();
    }

    private void removeItemFromCartUpdateDb(long id, Cart cart, CartItem itemToDelete) {
        cart.getCartItems().remove(itemToDelete);
        Item item = itemRepository.getReferenceById(id);
        item.setAvailable(item.getAvailable() + itemToDelete.getQuantity());
        itemRepository.save(item);
    }

    private Cart getCartFromSession() {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            throw new RuntimeException("Cart is empty");
        }
        return cart;
    }

    private static CartItem getCartItemFromCart(long id, Cart cart) {
        return cart.getCartItems().stream().filter(cartItem -> cartItem.getId() == id).findFirst()
                .orElseThrow(() -> new ItemException(String.format("Item with id: %d doesn't exist in cart", id)));
    }
}
