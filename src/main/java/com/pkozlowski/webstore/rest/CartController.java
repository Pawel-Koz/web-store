package com.pkozlowski.webstore.rest;

import com.pkozlowski.webstore.model.Cart;
import com.pkozlowski.webstore.model.dto.CartItemDto;
import com.pkozlowski.webstore.model.dto.Checkout;
import com.pkozlowski.webstore.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart/items")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<String> addToCart(@RequestBody CartItemDto request) {
        cartService.addItemToCart(request.getItemId(), request.getQuantity());
        return ResponseEntity.ok("Item added to cart");
    }

    @GetMapping
    public ResponseEntity<Cart> showCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeCartItem(@PathVariable long id) {
        cartService.removeCartItem(id);
        return ResponseEntity.ok("Item removed from cart");
    }

    @PutMapping
    public ResponseEntity<String> updateItem(@RequestBody CartItemDto cartItemDto) {
        cartService.updateCartItem(cartItemDto.getItemId(), cartItemDto.getQuantity());
        return ResponseEntity.ok("item updated");
    }

    @GetMapping("/checkout")
    public ResponseEntity<Checkout> checkout() {
        return ResponseEntity.ok(cartService.checkout());
    }
}
