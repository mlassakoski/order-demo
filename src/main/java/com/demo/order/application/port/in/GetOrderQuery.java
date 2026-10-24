package com.demo.order.application.port.in;

import com.demo.order.domain.OrderId;
import java.util.Objects;

/** Which order a customer wants to see. */
public record GetOrderQuery(OrderId orderId) {

    public GetOrderQuery {
        Objects.requireNonNull(orderId, "order id");
    }
}
