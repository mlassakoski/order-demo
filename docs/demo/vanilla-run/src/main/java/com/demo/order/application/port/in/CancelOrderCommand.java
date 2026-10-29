package com.demo.order.application.port.in;

import com.demo.order.domain.InvalidIdentifierException;
import com.demo.order.domain.OrderId;

/** Which order a customer wants to cancel. */
public record CancelOrderCommand(OrderId orderId) {

    public CancelOrderCommand {
        if (orderId == null) {
            throw new InvalidIdentifierException("Order id");
        }
    }
}
