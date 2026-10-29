package com.demo.order.domain;

/** An external identifier (customer, product) must not exceed its maximum length. */
public final class IdentifierTooLongException extends DomainValidationException {

    private static final long serialVersionUID = 1L;

    public IdentifierTooLongException(String identifierName, int maxLength) {
        super(identifierName + " must be at most " + maxLength + " characters");
    }
}
