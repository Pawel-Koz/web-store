package com.pkozlowski.webstore.model;

import lombok.*;

import java.math.BigDecimal;



@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private long ordinal;
    private long id;
    private String title;
    private BigDecimal pricePerUnit;
    private BigDecimal price;
    private int quantity;
}
