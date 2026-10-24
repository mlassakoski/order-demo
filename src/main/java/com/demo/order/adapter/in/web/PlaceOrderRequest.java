package com.demo.order.adapter.in.web;

import com.demo.order.application.port.in.PlaceOrderCommand;
import com.demo.order.domain.CustomerId;
import java.util.List;

/** Body of {@code POST /orders}. The client never sends an order identifier. */
public record PlaceOrderRequest(String customerId, List<OrderLineRequest> lines) {

    PlaceOrderCommand toCommand() {
        return new PlaceOrderCommand(
                new CustomerId(customerId),
                lines.stream().map(OrderLineRequest::toDomain).toList());
    }
}
