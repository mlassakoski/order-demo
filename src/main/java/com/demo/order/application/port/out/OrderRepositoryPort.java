package com.demo.order.application.port.out;

import com.demo.order.domain.Order;
import com.demo.order.domain.OrderId;
import java.util.Optional;

/** Output port: order storage. */
public interface OrderRepositoryPort {

    void save(Order order);

    Optional<Order> findById(OrderId id);
}
