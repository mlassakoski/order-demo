package com.demo.order.domain;

/** An order can have at most {@link Order#MAX_LINES} order lines. */
public final class TooManyOrderLinesException extends DomainValidationException {

    private static final long serialVersionUID = 1L;

    public TooManyOrderLinesException() {
        super("An order can have at most " + Order.MAX_LINES + " order lines");
    }
}
