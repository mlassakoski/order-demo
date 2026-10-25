package com.demo.order.domain;

/** One product and its quantity inside an order. */
public record OrderLine(ProductId productId, int quantity) {

    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 1000;

    public OrderLine {
        if (productId == null) {
            throw new InvalidIdentifierException("Product id");
        }
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new InvalidQuantityException();
        }
    }
}
