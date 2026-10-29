package com.demo.order.domain;

/** An order identifier must be a UUID in canonical (lowercase, hyphenated) form. */
public final class InvalidOrderIdException extends DomainValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidOrderIdException() {
        super("Order id must be a UUID");
    }
}
