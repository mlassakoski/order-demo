package com.demo.order.application.port.in;

import com.demo.order.domain.InvalidIdentifierException;
import com.demo.order.domain.OrderId;

/** Which order a customer wants to see. */
public record GetOrderQuery(OrderId orderId) {

    public GetOrderQuery {
        if (orderId == null) {
            throw InvalidIdentifierException.missing("Order id");
        }
    }
}
