package com.demo.order.domain;

/** An external identifier (customer, product) must be present and not blank. */
public final class InvalidIdentifierException extends DomainValidationException {

    public InvalidIdentifierException(String identifierName) {
        super(identifierName + " must not be blank");
    }
}
