package com.pkozlowski.webstore.model.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private long itemId;
    private int quantity;
}
