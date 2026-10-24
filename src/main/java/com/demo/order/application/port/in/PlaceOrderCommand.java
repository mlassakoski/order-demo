package com.demo.order.application.port.in;

import com.demo.order.domain.CustomerId;
import com.demo.order.domain.OrderLine;
import java.util.List;

/** What a customer sends to place an order. */
public record PlaceOrderCommand(CustomerId customerId, List<OrderLine> lines) {

    public PlaceOrderCommand {
        lines = lines == null ? null : List.copyOf(lines);
    }
}
