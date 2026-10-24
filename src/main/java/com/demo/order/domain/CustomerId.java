package com.demo.order.domain;

import java.util.Objects;

/** External identifier of the customer who places an order. */
public record CustomerId(String value) {

    public CustomerId {
        Objects.requireNonNull(value, "customer id");
    }
}
