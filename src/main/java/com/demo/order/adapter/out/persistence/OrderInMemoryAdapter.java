package com.demo.order.adapter.out.persistence;

import com.demo.order.application.port.out.OrderRepositoryPort;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Keeps orders in memory. Stores and returns copies, so changes persist only through {@link #save}. */
@Component
public class OrderInMemoryAdapter implements OrderRepositoryPort {

    private final Map<OrderId, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        orders.put(order.id(), copyOf(order));
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(orders.get(id)).map(OrderInMemoryAdapter::copyOf);
    }

    private static Order copyOf(Order order) {
        return Order.rehydrate(order.id(), order.customerId(), order.lines(), order.status());
    }
}
