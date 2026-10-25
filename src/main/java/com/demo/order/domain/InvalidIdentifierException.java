package com.demo.order.domain;

/** An external identifier (customer, product) must be present and not blank. */
public final class InvalidIdentifierException extends DomainValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidIdentifierException(String identifierName) {
        super(identifierName + " must not be blank");
    }
}
