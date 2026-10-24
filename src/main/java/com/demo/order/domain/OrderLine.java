package com.demo.order.domain;

import java.util.Objects;

/** One product and its quantity inside an order. */
public record OrderLine(ProductId productId, int quantity) {

    public OrderLine {
        Objects.requireNonNull(productId, "product id");
    }
}
