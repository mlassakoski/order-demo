package com.demo.order.application.service;

import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory fake of the order storage port, shared by the use case tests. Like the real adapter, it stores and returns
 * separate instances, so changes persist only through {@link #save}.
 */
final class FakeOrderRepository implements OrderRepositoryPort {

    private final Map<OrderId, Order> orders = new HashMap<>();

    @Override
    public void save(Order order) {
        orders.put(order.id(), copyOf(order));
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(orders.get(id)).map(FakeOrderRepository::copyOf);
    }

    boolean isEmpty() {
        return orders.isEmpty();
    }

    private static Order copyOf(Order order) {
        return Order.rehydrate(order.id(), order.customerId(), order.lines(), order.status());
    }
}
