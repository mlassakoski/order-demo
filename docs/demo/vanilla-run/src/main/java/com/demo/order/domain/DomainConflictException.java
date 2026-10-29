package com.demo.order.domain;

/** A request that the domain refuses because of the current state. Its message is safe to show to the caller. */
public abstract class DomainConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected DomainConflictException(String message) {
        super(message);
    }
}
