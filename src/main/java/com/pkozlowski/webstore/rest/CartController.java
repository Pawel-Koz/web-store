package com.pkozlowski.webstore.rest;

import com.pkozlowski.webstore.model.Cart;
import com.pkozlowski.webstore.model.dto.CartItemDto;
import com.pkozlowski.webstore.model.dto.Checkout;
import com.pkozlowski.webstore.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public String addToCart(@RequestBody CartItemDto request) {
        cartService.addItemToCart(request.getItemId(), request.getQuantity());
        return String.format("Added to cart: itemId: %d, quantity: %d", request.getItemId(), request.getQuantity());
    }

    @GetMapping("/")
    public Cart showCart() {
        return cartService.getCart();
    }

    @DeleteMapping("/{id}")
    public void removeCartItem(@PathVariable long id) {
        cartService.removeCartItem(id);
    }

    @PutMapping("/update")
    public String updateItem( @RequestBody CartItemDto cartItemDto) {
        cartService.updateItem(cartItemDto.getItemId(), cartItemDto.getQuantity());
        return "item updated";
    }

    @GetMapping("/checkout")
    public Checkout checkout() {
       return cartService.checkout();
    }


}
