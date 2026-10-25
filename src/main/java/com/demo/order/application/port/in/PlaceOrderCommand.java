package com.demo.order.application.port.in;

import com.demo.order.domain.CustomerId;
import com.demo.order.domain.EmptyOrderException;
import com.demo.order.domain.InvalidIdentifierException;
import com.demo.order.domain.InvalidOrderLineException;
import com.demo.order.domain.OrderLine;
import java.util.List;
import java.util.Objects;

/**
 * What a customer sends to place an order. Refuses missing parts with the same domain exceptions
 * the {@code Order} aggregate throws; the aggregate stays the source of truth for every other rule.
 */
public record PlaceOrderCommand(CustomerId customerId, List<OrderLine> lines) {

    public PlaceOrderCommand {
        if (customerId == null) {
            throw new InvalidIdentifierException("Customer id");
        }
        if (lines == null) {
            throw new EmptyOrderException();
        }
        if (lines.stream().anyMatch(Objects::isNull)) {
            throw new InvalidOrderLineException();
        }
        lines = List.copyOf(lines);
    }
}
