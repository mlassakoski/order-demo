package com.demo.order.adapter.in.web;

import com.demo.order.application.port.in.NotFoundException;

/**
 * The order id in the request path can never name an order (it is not a canonical UUID). Its message never contains
 * the raw value, so nothing the client typed is reflected back.
 */
final class MalformedOrderIdException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    MalformedOrderIdException() {
        super("Order was not found");
    }
}
