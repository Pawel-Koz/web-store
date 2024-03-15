package com.pkozlowski.webstore.service.impl;

import com.pkozlowski.webstore.exception.ItemException;
import com.pkozlowski.webstore.model.Cart;
import com.pkozlowski.webstore.model.CartItem;
import com.pkozlowski.webstore.model.Item;
import com.pkozlowski.webstore.repository.ItemRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private HttpSession session;
    @InjectMocks
    private CartServiceImpl cartService;

    private Item item;

    @BeforeEach
    void setup() {
        this.item = new Item("saw", 10, new BigDecimal(100));
    }

    @Test
    void addItemToCart_should_throw_exception_when_item_not_found_in_db() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());
        Exception thrown = assertThrows(ItemException.class, () -> cartService.addItemToCart(1L, 1));
        assertEquals("Item with id: 1 not available", thrown.getMessage());
    }

    @Test
    void addItemToCart_should_throw_exception_when_quantity_is_greater_than_available() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        Exception thrown = assertThrows(RuntimeException.class, () -> cartService.addItemToCart(1L, 11));
        assertEquals(String.format("Available item: %s - number of items: %d, requested:%d",
                item.getTitle(), item.getAvailable(), 11), thrown.getMessage());
    }

    @Test
    void addItemToCart_should_create_new_cart_when_none_in_session() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(session.getAttribute(anyString())).thenReturn(null);
        cartService.addItemToCart(1L, 10);

        verify(session, times(1)).getAttribute("cart");
        verify(session, times(1)).setAttribute(eq("cart"), any(Cart.class));
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void addItemToCart_should_add_cartItem_when_cart_exists_in_session() {
        Cart cart = new Cart();
        CartItem cartItem = new CartItem(0, 1L, "saw", new BigDecimal(100), new BigDecimal(1000), 10);
        cart.addCartItem(cartItem);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(session.getAttribute(anyString())).thenReturn(cart);

        cartService.addItemToCart(1L, 10);
        verify(session, times(1)).getAttribute("cart");
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void getCart_should_return_empty_new_Cart_when_none_in_session() {
        when(session.getAttribute(anyString())).thenReturn(null);
        Cart actual = cartService.getCart();
        assertEquals(new Cart(), actual);
    }

    @Test
    void getCart_should_return_cart_stored_in_session() {
        Cart cart = new Cart();
        CartItem cartItem = new CartItem(1, 1L, "saw", new BigDecimal(100), new BigDecimal(1000), 10);
        cart.addCartItem(cartItem);
        when(session.getAttribute(anyString())).thenReturn(cart);
        Cart actual = cartService.getCart();
        assertFalse(actual.getCartItems().isEmpty());
        assertEquals(new BigDecimal(1000), actual.getSubtotal());
        assertEquals(cartItem, actual.getCartItems().get(0));
    }

    @Test
    void removeCartItem_should_throw_exception_when_no_cart_in_session() {
        when(session.getAttribute(anyString())).thenReturn(null);
        Exception thrown = assertThrows(RuntimeException.class, () -> cartService.removeCartItem(1L));
        assertEquals("Cart is empty", thrown.getMessage());
    }

    @Test
    void removeCartItem_should_throw_exception_when_no_cartItem_in_cart() {
        Cart cart = new Cart();
        when(session.getAttribute(anyString())).thenReturn(cart);
        Exception thrown = assertThrows(RuntimeException.class, () -> cartService.removeCartItem(1L));
        assertEquals("Item with id: 1 doesn't exist in cart", thrown.getMessage());
    }

    @Test
    void removeCartItem_should_remove_cartItem_from_cart() {
        Cart cart = new Cart();
        CartItem cartItem = new CartItem(1, 1L, "saw", new BigDecimal(100), new BigDecimal(1000), 10);
        cart.addCartItem(cartItem);
        when(session.getAttribute(anyString())).thenReturn(cart);
        when(itemRepository.getReferenceById(anyLong())).thenReturn(item);
        cartService.removeCartItem(1L);

        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void updateItem_should_remove_item_when_quantity_0() {
        Cart cart = new Cart();
        CartItem cartItem = new CartItem(1, 1L, "saw", new BigDecimal(100), new BigDecimal(1000), 10);
        cart.addCartItem(cartItem);
        when(session.getAttribute(anyString())).thenReturn(cart);
        when(itemRepository.getReferenceById(anyLong())).thenReturn(item);
        cartService.updateCartItem(1L, 0);
        verify(itemRepository, times(2)).save(item);
    }

    @Test
    void updateItem_should_remove_item_when_quantity_is_greater_then_actual() {
        Cart cart = new Cart();
        CartItem cartItem = new CartItem(1, 1L, "saw", new BigDecimal(100), new BigDecimal(1000), 10);
        cart.addCartItem(cartItem);
        when(session.getAttribute(anyString())).thenReturn(cart);
        when(itemRepository.getReferenceById(anyLong())).thenReturn(item);
        cartService.updateCartItem(1L, 11);
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void updateItem_should_remove_item_when_quantity_is_lesser_than_actual() {
        Cart cart = new Cart();
        CartItem cartItem = new CartItem(1, 1L, "saw", new BigDecimal(100), new BigDecimal(1000), 10);
        cart.addCartItem(cartItem);
        when(session.getAttribute(anyString())).thenReturn(cart);
        when(itemRepository.getReferenceById(anyLong())).thenReturn(item);
        cartService.updateCartItem(1L, 9);
        verify(itemRepository, times(1)).save(item);
    }

}