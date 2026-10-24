package com.demo.order.application.port.in;

import com.demo.order.domain.OrderId;

/** No order exists with the given identifier. */
public class OrderNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public OrderNotFoundException(OrderId id) {
        super("Order " + id.value() + " was not found");
    }
}
