package com.pkozlowski.webstore.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String title;
    private int available;
    private BigDecimal price;

    public Item(String title, int available, BigDecimal price) {
        this.title = title;
        this.available = available;
        this.price = price;
    }
}
