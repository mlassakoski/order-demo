package com.demo.order.application.service;

import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** In-memory fake of the order storage port, shared by the use case tests. */
final class FakeOrderRepository implements OrderRepositoryPort {

    private final Map<OrderId, Order> orders = new HashMap<>();

    @Override
    public void save(Order order) {
        orders.put(order.id(), order);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(orders.get(id));
    }

    boolean isEmpty() {
        return orders.isEmpty();
    }
}
