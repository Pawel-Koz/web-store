package com.pkozlowski.webstore.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckedItem {
    private String title;
    private int quantity;
    private BigDecimal totalPrice;
    private Status status;

    public CheckedItem(String title) {
        this.title = title;
        this.status = Status.AVAILABLE;
    }

    public enum Status {
        AVAILABLE,
        NOT_AVAILABLE
    }
}


