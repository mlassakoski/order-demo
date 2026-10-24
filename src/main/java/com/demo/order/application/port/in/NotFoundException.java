package com.demo.order.application.port.in;

/** A requested aggregate does not exist. The web adapter maps every subclass to HTTP 404. */
public abstract class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected NotFoundException(String message) {
        super(message);
    }
}
