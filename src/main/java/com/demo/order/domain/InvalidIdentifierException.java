package com.demo.order.domain;

/** An identifier (customer, product, order) must be present and not blank. */
public final class InvalidIdentifierException extends DomainValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidIdentifierException(String identifierName) {
        this(identifierName, "must not be blank");
    }

    private InvalidIdentifierException(String identifierName, String problem) {
        super(identifierName + " " + problem);
    }

    /** The identifier is absent altogether ({@code null}), not merely blank. */
    public static InvalidIdentifierException missing(String identifierName) {
        return new InvalidIdentifierException(identifierName, "must not be null");
    }
}
