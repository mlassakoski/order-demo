package com.demo.order.domain;

import java.util.List;
import java.util.Objects;

/** A customer's request to buy one or more products, tracked through its lifecycle. */
public final class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private final OrderStatus status;

    private Order(OrderId id, CustomerId customerId, List<OrderLine> lines, OrderStatus status) {
        this.id = Objects.requireNonNull(id, "order id");
        this.customerId = Objects.requireNonNull(customerId, "customer id");
        this.lines = List.copyOf(Objects.requireNonNull(lines, "order lines"));
        this.status = Objects.requireNonNull(status, "order status");
    }

    /** Places a new order: it gets a fresh identifier and starts as Pending. */
    public static Order place(CustomerId customerId, List<OrderLine> lines) {
        return new Order(OrderId.newId(), customerId, lines, OrderStatus.PENDING);
    }

    /** Rebuilds an order from storage, keeping its identifier and state. */
    public static Order rehydrate(OrderId id, CustomerId customerId, List<OrderLine> lines, OrderStatus status) {
        return new Order(id, customerId, lines, status);
    }

    public OrderId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    public OrderStatus status() {
        return status;
    }
}
