package com.demo.order.domain;

/** External identifier of the customer who places an order. */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new InvalidIdentifierException("Customer id");
        }
    }
}
