package com.demo.order.domain;

import java.util.Objects;

/** External identifier of a product a customer can order. */
public record ProductId(String value) {

    public ProductId {
        Objects.requireNonNull(value, "product id");
    }
}
