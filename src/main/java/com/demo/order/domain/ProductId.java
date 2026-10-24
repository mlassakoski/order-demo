package com.demo.order.domain;

/** External identifier of a product a customer can order. */
public record ProductId(String value) {

    public ProductId {
        if (value == null || value.isBlank()) {
            throw new InvalidIdentifierException("Product id");
        }
    }
}
