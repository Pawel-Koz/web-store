package com.pkozlowski.webstore.service.impl;

import com.pkozlowski.webstore.exception.ItemException;
import com.pkozlowski.webstore.model.*;
import com.pkozlowski.webstore.model.dto.Checkout;
import com.pkozlowski.webstore.repository.ItemRepository;
import com.pkozlowski.webstore.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
        final Cart finalCart = cart;
        cart.getCartItems().stream().filter(itemInCart -> itemInCart.getTitle().equals(item.getTitle()))
                .findFirst()
                .ifPresentOrElse(
                        cartItem -> updateCartItem(quantity, cartItem, ItemOperation.ADD),
                        () -> {
                            CartItem cartItem = buildCartItem(quantity, item);
                            finalCart.addCartItem(cartItem);
                        });
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
        for (CartItem cartItem : cart.getCartItems()) {
            cartItem.setOrdinal(ordinal++);
            total = total.add(cartItem.getPrice());
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
            quantityToSet = updateCartItem(quantity, cartItem, ItemOperation.DECREASE);
            item.setAvailable(item.getAvailable() + quantityToSet);
        } else if (quantity > actualItemsQuantity) {
            quantityToSet = updateCartItem(quantity, cartItem, ItemOperation.INCREASE);
            item.setAvailable(item.getAvailable() - quantityToSet);
        }
        itemRepository.save(item);
    }

    @Override
    public Checkout checkout() {
        Cart cart = getCartFromSession();
        Checkout checkout = new Checkout();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            Item item = itemRepository.getReferenceById(cartItem.getId());
            CheckedItem checkedItem = new CheckedItem(cartItem.getTitle());
            if(cartItem.getQuantity() > item.getAvailable()) {
                checkedItem.setStatus(CheckedItem.Status.NOT_AVAILABLE);
            }
            checkedItem.setQuantity(cartItem.getQuantity());
            checkedItem.setTotalPrice(cartItem.getPrice());
            checkout.addCheckedItem(checkedItem);
            if(checkedItem.getStatus() == CheckedItem.Status.AVAILABLE) {
                total = total.add(checkedItem.getTotalPrice());
            }
        }
        checkout.setTotal(total);
        return checkout;
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

    private static int updateCartItem(int quantity, CartItem cartItem, ItemOperation operation) {
        int setQuantity;
        switch (operation) {
            case ADD -> {
                setQuantity = cartItem.getQuantity() + quantity;
                cartItem.setQuantity(setQuantity);
                cartItem.setPrice(cartItem.getPricePerUnit().multiply(new BigDecimal(setQuantity)));
            }
            case INCREASE -> {
                setQuantity = quantity - cartItem.getQuantity();
                cartItem.setQuantity(quantity);
                cartItem.setPrice(cartItem.getPricePerUnit().multiply(new BigDecimal(quantity)));
            }
            case DECREASE -> {
                setQuantity = cartItem.getQuantity() - quantity;
                cartItem.setQuantity(quantity);
                cartItem.setPrice(cartItem.getPricePerUnit().multiply(new BigDecimal(quantity)));
            }
            default -> throw new RuntimeException(String.format("Unknown operation %s", operation));
        }
        return setQuantity;
    }
}
