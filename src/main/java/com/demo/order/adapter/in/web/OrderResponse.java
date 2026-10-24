package com.demo.order.adapter.in.web;

import com.demo.order.domain.Order;
import java.util.List;

/** An order as returned over HTTP. */
public record OrderResponse(String id, String customerId, List<OrderLineResponse> lines, String status) {

    /** One order line as returned over HTTP. */
    public record OrderLineResponse(String productId, int quantity) {}

    static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id().value(),
                order.customerId().value(),
                order.lines().stream()
                        .map(line -> new OrderLineResponse(line.productId().value(), line.quantity()))
                        .toList(),
                order.status().name());
    }
}
