package com.demo.order.domain;

/**
 * An operation the aggregate's current state does not allow. Its message is safe to show to the caller, and
 * {@link #currentState()} names the state that forbids it.
 */
public abstract class DomainRuleViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected DomainRuleViolationException(String message) {
        super(message);
    }

    /** The state the aggregate is in, which forbids the operation. */
    public abstract Enum<?> currentState();
}
