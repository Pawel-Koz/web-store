package com.pkozlowski.webstore.model;

public enum OrderStatus {
   ORDERED("accepted"),
    READY("ready"),
    DELIVERY("shipping");

    OrderStatus(String description) {
    }
}
