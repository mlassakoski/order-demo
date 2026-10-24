package com.demo.order.domain;

/** Input that breaks a domain invariant. Its message is safe to show to the caller. */
public abstract class DomainValidationException extends RuntimeException {

    protected DomainValidationException(String message) {
        super(message);
    }
}
