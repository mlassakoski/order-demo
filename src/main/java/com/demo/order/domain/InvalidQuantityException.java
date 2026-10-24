package com.demo.order.domain;

/** The quantity of an order line must be between {@link OrderLine#MIN_QUANTITY} and {@link OrderLine#MAX_QUANTITY}. */
public final class InvalidQuantityException extends DomainValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidQuantityException() {
        super("Quantity must be between " + OrderLine.MIN_QUANTITY + " and " + OrderLine.MAX_QUANTITY);
    }
}
