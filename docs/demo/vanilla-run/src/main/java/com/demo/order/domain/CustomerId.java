package com.demo.order.domain;

/** External identifier of the customer who places an order. */
public record CustomerId(String value) {

    public static final int MAX_LENGTH = 64;

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new InvalidIdentifierException("Customer id");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IdentifierTooLongException("Customer id", MAX_LENGTH);
        }
    }
}
