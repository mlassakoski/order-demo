package com.demo.order.domain;

import java.util.List;
import java.util.Objects;

/** A customer's request to buy one or more products, tracked through its lifecycle. */
public final class Order {

    public static final int MAX_LINES = 100;

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private final OrderStatus status;

    private Order(OrderId id, CustomerId customerId, List<OrderLine> lines, OrderStatus status) {
        if (id == null) {
            throw new InvalidIdentifierException("Order id");
        }
        if (customerId == null) {
            throw new InvalidIdentifierException("Customer id");
        }
        if (status == null) {
            throw new MissingOrderStatusException();
        }
        this.id = id;
        this.customerId = customerId;
        this.lines = validLines(lines);
        this.status = status;
    }

    private static List<OrderLine> validLines(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new EmptyOrderException();
        }
        if (lines.size() > MAX_LINES) {
            throw new TooManyOrderLinesException();
        }
        if (lines.stream().anyMatch(Objects::isNull)) {
            throw new InvalidOrderLineException();
        }
        return List.copyOf(lines);
    }

    /** Places a new order: it gets a fresh identifier and starts as Pending. */
    public static Order place(CustomerId customerId, List<OrderLine> lines) {
        return new Order(OrderId.newId(), customerId, lines, OrderStatus.PENDING);
    }

    /** Rebuilds an order from storage, keeping its identifier and state. */
    public static Order rehydrate(OrderId id, CustomerId customerId, List<OrderLine> lines, OrderStatus status) {
        return new Order(id, customerId, lines, status);
    }

    /** Returns this order as Cancelled. Only a Pending or Paid order can be cancelled. */
    public Order cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.PAID) {
            throw new OrderNotCancellableException(status);
        }
        return new Order(id, customerId, lines, OrderStatus.CANCELLED);
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
