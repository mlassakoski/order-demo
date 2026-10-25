package com.demo.order.domain;

/** External identifier of a product a customer can order. */
public record ProductId(String value) {

    public static final int MAX_LENGTH = 64;

    public ProductId {
        if (value == null || value.isBlank()) {
            throw new InvalidIdentifierException("Product id");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IdentifierTooLongException("Product id", MAX_LENGTH);
        }
    }
}
