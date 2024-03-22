package com.pkozlowski.webstore.service.impl;

import com.pkozlowski.webstore.exception.ItemException;
import com.pkozlowski.webstore.exception.UserException;
import com.pkozlowski.webstore.mapper.ItemMapper;
import com.pkozlowski.webstore.model.*;
import com.pkozlowski.webstore.model.dto.Checkout;
import com.pkozlowski.webstore.repository.ItemRepository;
import com.pkozlowski.webstore.repository.OrderRepository;
import com.pkozlowski.webstore.repository.UserRepository;
import com.pkozlowski.webstore.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CartServiceImpl implements CartService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final HttpSession session;
    private final ItemMapper itemMapper;

    public CartServiceImpl(ItemRepository itemRepository, OrderRepository orderRepository,
                           UserRepository userRepository,
                           HttpSession httpSession, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.session = httpSession;
        this.itemMapper = itemMapper;
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
        removeItemFromCartUpdateDb(cart, itemToDelete);
    }

    @Override
    public void updateCartItem(long id, int quantity) {
        Cart cart = getCartFromSession();
        CartItem cartItem = getCartItemFromCart(id, cart);
        if (quantity == 0) {
            removeItemFromCartUpdateDb(cart, cartItem);
        } else {
            updateCartItem(quantity, cartItem, ItemOperation.UPDATE);
        }
    }

    @Override
    public Checkout checkout() {
        Cart cart = getCartFromSession();
        Checkout checkout = new Checkout();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            Item item = itemRepository.getReferenceById(cartItem.getId());
            CheckedItem checkedItem = itemMapper.toCheckedItem(cartItem);
            if (cartItem.getQuantity() > item.getAvailable()) {
                checkedItem.setStatus(CheckedItem.Status.NOT_AVAILABLE);
            } else {
                total = total.add(cartItem.getPrice());
                item.setAvailable(item.getAvailable() - cartItem.getQuantity());
                itemRepository.save(item);
            }
            checkout.addCheckedItem(checkedItem);
        }
        checkout.setTotal(total);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(
                () -> new UserException(String.format("User with email: %s does not exist", userDetails.getUsername()))
        );
        saveOrder(total, user, orderRepository);
        return checkout;
    }

    private void saveOrder(BigDecimal total, User user, OrderRepository repository) {
        Order order = Order.builder()
                .timestamp(Date.valueOf(LocalDate.now()))
                .total(total)
                .status(OrderStatus.ORDERED)
                .user(user)
                .build();
        repository.save(order);

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

    private void removeItemFromCartUpdateDb(Cart cart, CartItem itemToDelete) {
        cart.getCartItems().remove(itemToDelete);
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

    private static void updateCartItem(int quantity, CartItem cartItem, ItemOperation operation) {
        switch (operation) {
            case ADD -> {
                int setQuantity = cartItem.getQuantity() + quantity;
                cartItem.setQuantity(setQuantity);
                cartItem.setPrice(cartItem.getPricePerUnit().multiply(new BigDecimal(setQuantity)));
            }
            case UPDATE -> {
                cartItem.setQuantity(quantity);
                cartItem.setPrice(cartItem.getPricePerUnit().multiply(new BigDecimal(quantity)));
            }
            default -> throw new RuntimeException(String.format("Unknown operation %s", operation));
        }
    }
}
