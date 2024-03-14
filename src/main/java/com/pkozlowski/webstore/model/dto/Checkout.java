package com.pkozlowski.webstore.model.dto;

import com.pkozlowski.webstore.model.CheckedItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Checkout {
    private List<CheckedItem> checkedItems;
    private BigDecimal total;

    public Checkout() {
        this.checkedItems = new ArrayList<>();
    }
    public void addCheckedItem(CheckedItem item) {
        checkedItems.add(item);
    }
}
